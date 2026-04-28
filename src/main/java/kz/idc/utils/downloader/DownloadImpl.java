package kz.idc.utils.downloader;

import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.Observable;
import kz.idc.dto.DownloadDTO;
import kz.idc.dto.audio.AudioDTO;
import kz.idc.dto.plan.PlanDTO;
import kz.idc.dto.settings.SettingsDTO;
import kz.idc.rs.services.client.api.requests.APIRequests;
import kz.idc.utils.file.$FileUtils;
import kz.idc.utils.file.FileTypes;
import kz.idc.utils.file.FileUtils;
import kz.idc.utils.xxh.$GenerateXXH;
import kz.idc.utils.xxh.GenerateXHH;
import kz.idc.ws.WebSocket;
import kz.idc.ws.WebSocketTopics;
import net.jpountz.xxhash.StreamingXXHash64;

import java.util.ArrayList;
import java.util.List;

import static kz.idc.utils.file.PathsEnum.AUDIO;
import static kz.idc.utils.file.PathsEnum.PLAN;

public final class DownloadImpl implements Download {

    private final RxHttpClient rxHttpClient;
    private final RxStreamingHttpClient rxStreamingHttpClient;
    private final FileUtils fileUtils;
    private final GenerateXHH generateXHH;
    private final APIRequests apiRequests;
    private static boolean isDownloadedAudios;
    private static boolean isDownloadedPlan;

    public DownloadImpl(RxHttpClient rxHttpClient,
                        RxStreamingHttpClient rxStreamingHttpClient,
                        APIRequests apiRequests) {
        this.rxHttpClient = rxHttpClient;
        this.rxStreamingHttpClient = rxStreamingHttpClient;
        this.apiRequests = apiRequests;
        this.fileUtils = $FileUtils.mk();
        this.generateXHH = $GenerateXXH.mk();
    }

    @Override
    public Observable<Object> downloadAudios(String uuid,
                                             String path,
                                             List<AudioDTO> audios,
                                             SettingsDTO settingsDTO,
                                             WebSocket webSocket) {
        if (!isDownloadedAudios) {
            isDownloadedAudios = true;
            return removeNotActuallyAudios(audios)
                    .flatMap(this::getOnlyNewAudios)
                    .flatMap(news -> downloadAudio(uuid, apiRequests, path, news, settingsDTO, webSocket));
        } else {
            return Observable.create(already -> {
                webSocket.onMessage(uuid, DownloadDTO.create(true, FileTypes.AUDIO.TYPE));
                already.onComplete();
            });
        }
    }

    @Override
    public Observable<Object> downloadPlan(String uuid,
                                           String path,
                                           PlanDTO plan,
                                           SettingsDTO settingsDTO,
                                           WebSocket webSocket) {
        if (!isDownloadedPlan) {
            isDownloadedPlan = true;
            final StreamingXXHash64 xxHash64 = generateXHH.init();
            final DownloadDTO downloadDTO = DownloadDTO.create(true, FileTypes.PLAN.TYPE);
            return Observable.create(e -> {
                e.onNext(fileUtils.clearDirectoryPlan());
                e.onComplete();
            }).flatMap(f -> rxStreamingHttpClient.dataStream(apiRequests.download(settingsDTO.getCentralServer(), path, plan.getName()))
                    .map(ByteBuffer::toByteArray)
                    .toObservable().flatMap(bytes -> Observable.create(s -> {
                                fileUtils.saveFile(plan.getName(), bytes, PLAN.PATH);
                                generateXHH.addByteToXXH64(bytes, xxHash64);
                                s.onComplete();
                            })
                    ).doOnComplete(() -> {
                        if (!generateXHH.getXXH64Checksum(xxHash64)
                                .equals(plan.getChecksum())) {
                            fileUtils.removeAudioFile(plan.getName());
                            System.out.println("Checksum plan not correct: " + plan.getName());
                            downloadDTO.setDownloaded(false);
                            webSocket.onMessage(uuid, downloadDTO);
                        } else {
                            System.out.println("Downloaded Plan: " + plan.getName());
                        }
                    })).doOnComplete(() -> {
                webSocket.onMessage(uuid, downloadDTO);
                if(!uuid.equals(WebSocketTopics.RESCUE.TOPIC) && !uuid.equals(WebSocketTopics.WELCOME.TOPIC)){
                    webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, downloadDTO);
                }
                isDownloadedPlan = false;
                rxHttpClient.exchange(apiRequests.updateDownloadStatusBackend(settingsDTO.getCentralServer(), settingsDTO.getRescue(), downloadDTO)).firstElement()
                        .onErrorResumeNext(throwable -> result -> {
                            result.onSuccess(HttpResponse.badRequest());
                        }).subscribe();
            });
        } else {
            return Observable.create(already -> {
                webSocket.onMessage(uuid, DownloadDTO.create(true, FileTypes.PLAN.TYPE));
                already.onComplete();
            });
        }
    }

    private Observable<List<AudioDTO>> getOnlyNewAudios(List<AudioDTO> audios) {
        return Observable.create(o -> {
            o.onNext(difference(audios, fileUtils.getAudioFileList()));
            o.onComplete();
        });
    }

    private Observable<Object> downloadAudio(String uuid,
                                             APIRequests apiRequests,
                                             String path,
                                             List<AudioDTO> audios,
                                             SettingsDTO settingsDTO,
                                             WebSocket webSocket) {
        final StreamingXXHash64 xxHash64 = generateXHH.init();
        final DownloadDTO downloadDTO = DownloadDTO.create(true, FileTypes.AUDIO.TYPE);
        return Observable.fromIterable(audios)
                .flatMap(audio -> {
                    fileUtils.removeAudioFile(audio.getName());
                    return rxStreamingHttpClient.dataStream(apiRequests.download(settingsDTO.getCentralServer(), path, audio.getName()))
                            .map(ByteBuffer::toByteArray)
                            .toObservable()
                            .flatMap(bytes -> Observable.create(s -> {
                                        fileUtils.saveFile(audio.getName(), bytes, AUDIO.PATH);
                                        generateXHH.addByteToXXH64(bytes, xxHash64);
                                        s.onComplete();
                                    })
                            ).doOnComplete(() -> {
                                /*if (!generateXHH.getXXH64Checksum(xxHash64)
                                        .equals(audio.getChecksum())) {
                                    fileUtils.removeAudioFile(audio.getName());
                                    System.out.println("Checksum audio not correct: " + audio.getName());
                                    downloadDTO.setDownloaded(false);
                                } else {
                                    System.out.println("Downloaded audio: " + audio.getName());
                                }*/
                                System.out.println("Downloaded audio: " + audio.getName());
                            });
                }).doOnComplete(() -> {
            webSocket.onMessage(uuid, downloadDTO);
            isDownloadedAudios = false;
            rxHttpClient.exchange(apiRequests.updateDownloadStatusBackend(settingsDTO.getCentralServer(), settingsDTO.getRescue(), downloadDTO)).firstElement()
                    .onErrorResumeNext(throwable -> result -> {
                        result.onSuccess(HttpResponse.badRequest());
                    }).subscribe();
        });
    }

    private Observable<List<AudioDTO>> removeNotActuallyAudios(List<AudioDTO> files) {
        return Observable.create(d -> {
            List<AudioDTO> diff = difference(fileUtils.getAudioFileList(), files);
            for (AudioDTO audioDTO :
                    diff) {
                fileUtils.removeAudioFile(audioDTO.getName());
            }
            d.onNext(files);
            d.onComplete();
        });
    }

    private List<AudioDTO> difference(List<AudioDTO> a, List<AudioDTO> b) {
        List<AudioDTO> differences = new ArrayList<>(a);
        differences.removeAll(b);
        return differences;
    }
}
