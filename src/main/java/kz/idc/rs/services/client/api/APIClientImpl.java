package kz.idc.rs.services.client.api;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.*;
import kz.idc.dto.EmergencyDTO;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.audio.AudiosDTO;
import kz.idc.dto.marquee.MarqueeDTO;
import kz.idc.dto.network.NetworkIPMacDTO;
import kz.idc.dto.plan.PlanDTO;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.dto.sip.acc.AccountDTO;
import kz.idc.error.$Error;
import kz.idc.error.Errors;
import kz.idc.rs.services.client.ac.ACTools;
import kz.idc.rs.services.client.network.NetworkTools;
import kz.idc.rs.services.client.sip.SipClient;
import kz.idc.rs.services.settings.SettingsService;
import kz.idc.utils.downloader.Download;
import kz.idc.utils.file.$FileUtils;
import kz.idc.utils.gpio.GPIO;
import kz.idc.utils.storage.Storage;
import kz.idc.ws.WebSocket;
import kz.idc.rs.services.client.api.requests.$APIRequests;
import kz.idc.rs.services.client.api.requests.APIConfiguration;
import kz.idc.rs.services.client.api.requests.APIRequests;
import kz.idc.utils.downloader.$Downloader;
import kz.idc.utils.storage.$Storage;
import kz.idc.ws.WebSocketTopics;

public class APIClientImpl implements APIClient {

    private final RxHttpClient httpClient;
    private final APIRequests apiRequests;
    private final NetworkTools networkTools;
    private final SipClient sipClient;
    private final ACTools acTools;
    private final Storage storage = $Storage.mk();
    private final Download downloader;

    public APIClientImpl(RxHttpClient httpClient,
                         RxStreamingHttpClient streamingHttpClient,
                         APIConfiguration configuration,
                         NetworkTools networkTools,
                         SipClient sipClient,
                         ACTools acTools) {
        this.httpClient = httpClient;
        this.networkTools = networkTools;
        this.acTools = acTools;
        this.sipClient = sipClient;
        apiRequests = $APIRequests.mk(configuration);
        downloader = $Downloader.mk(httpClient, streamingHttpClient, apiRequests);
    }

    @Override
    public Maybe<Object> getRegRescueId(ServerAddressDTO centralServer) {
        return storage.getSettings().toMaybe().flatMap(settingsDTO ->
                networkTools.getIpFromInterface(settingsDTO.isVpnNetworkEnabled())
                        .flatMap(ip -> httpClient.retrieve(apiRequests
                                        .getRegRescueId(centralServer, (NetworkIPMacDTO) ip), RescueDTO.class)
                                .firstElement()
                                .flatMap(rescue -> storage.setRescueId(rescue.getId()).toMaybe())
                                .flatMap(rescue -> storage.setCentralServer(centralServer).toMaybe())
                                .flatMap(get -> storage.getRescueId().toMaybe()))
                        .flatMap(rescue -> result -> result.onSuccess(rescue))
                        .onErrorResumeNext(t -> er -> {
                            if (t.getMessage() != null) {
                                er.onSuccess($Error.mk().createError(t.getMessage()));
                            } else {
                                er.onSuccess($Error.mk().createError(Errors.NULL_POINTER_NETWORK_DEVICE.EXCEPTION));
                            }
                        }));
    }

    @Override
    public Maybe<Object> getAudios(String uuid, WebSocket webSocket) {
        return storage.getSettings().toMaybe()
                .flatMap(settingsDTO -> httpClient.retrieve(apiRequests.getAudios(settingsDTO.getCentralServer()),
                                AudiosDTO.class)
                        .firstElement()
                        .doAfterSuccess(audiosDTO -> downloader.downloadAudios(uuid, audiosDTO.getPath(),
                                audiosDTO.getAudios(), settingsDTO, webSocket).subscribe())
                        .flatMap(audiosDTO -> result -> result.onSuccess(HttpResponse.ok())))
                .onErrorResumeNext(t -> result -> {
                    System.out.println(t.getMessage());
                    result.onSuccess($Error.mk().createError(t.getMessage()));
                });
    }

    @Override
    public Maybe<Object> getPlan(String uuid, WebSocket webSocket) {
        return storage.getSettings().toMaybe()
                .flatMap(settings -> httpClient.retrieve(apiRequests.getPlan(settings.getCentralServer(),
                                settings.getRescue()), PlanDTO.class)
                        .firstElement()
                        .doAfterSuccess(plan ->
                                downloader
                                        .downloadPlan(uuid, plan.getPath(), plan, settings, webSocket)
                                        .subscribe())
                        .flatMap(plan -> result -> result.onSuccess(HttpResponse.ok())))
                .onErrorResumeNext(t -> result -> {
                    System.out.println(t.getMessage());
                    result.onSuccess($Error.mk().createErrorPlan(t.getMessage()));
                });
    }

    @Override
    public Maybe<Object> getMarquees(WebSocket webSocket) {
        return storage.getSettings().toMaybe()
                .flatMap(settings -> httpClient.retrieve(apiRequests.getMarquees(settings.getCentralServer()), MarqueeDTO[].class)
                        .firstElement()
                        .flatMap(marqueeDTOS -> result -> {
                            String s = " ";
                            for (MarqueeDTO marquee :
                                    marqueeDTOS) {
                                s += marquee.getMarquee() + " ";
                            }
                            MarqueeDTO marquee = MarqueeDTO.create(s);
                            webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, marquee);
                            result.onSuccess(HttpResponse.ok());
                        }).doAfterSuccess(audiosDTO -> httpClient.exchange(apiRequests.setMarqueeStatus(settings.getRescue().getId(), settings.getCentralServer(), StatusDTO.create(true))).subscribe())
                ).onErrorResumeNext(t -> result -> {
                    webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, MarqueeDTO.create(" "));
                    result.onSuccess(HttpResponse.ok());
                });
    }

    @Override
    public Maybe<Object> getRegSipAccount(AccWithRegServerDTO accWithRegServerDTO) {
        
        return storage.getSipAccountWithRegServer().toMaybe()
                .flatMap(sip -> create -> {
                    if (!sip.equals(accWithRegServerDTO)) {
                        if (!accWithRegServerDTO.getAccount().getAccount().isBlank()
                                && accWithRegServerDTO.getAccount().getPassword().isBlank()
                                && accWithRegServerDTO.getRegServer().getHostname().equals(sip.getRegServer().getHostname())
                                && accWithRegServerDTO.getRegServer().getPort() == sip.getRegServer().getPort()) {
                            create.onSuccess(AccountDTO.create(sip.getAccount().getAccount(), sip.getAccount().getPassword()));
                        } else {
                            sip.getRegServer().setHostname(accWithRegServerDTO.getRegServer().getHostname());
                            sip.getRegServer().setPort(accWithRegServerDTO.getRegServer().getPort());
                            create.onError(new Throwable());
                        }
                    } else {
                        create.onSuccess(AccountDTO.create(sip.getAccount().getAccount(), sip.getAccount().getPassword()));
                    }
                })
                .onErrorResumeNext(Maybe.create(t -> {
                            if (accWithRegServerDTO.getAccount().getAccount().isBlank()) {
                                t.onSuccess(apiRequests.createRegSipId(accWithRegServerDTO));
                            } else {
                                t.onSuccess(apiRequests.updateRegSipId(accWithRegServerDTO));
                            }
                        })
                        .flatMap(req -> httpClient.retrieve((HttpRequest<?>) req, AccountDTO.class).firstElement()
                                .flatMap(account -> update -> {
                                    accWithRegServerDTO.setAccount(account);
                                    update.onSuccess(accWithRegServerDTO);
                                })
                                .doAfterSuccess(s -> storage.setSipAccountWithRegServer(accWithRegServerDTO).subscribe()))
                        .flatMap(sipAcc -> sipClient.updateSipAccount(accWithRegServerDTO)
                                .flatMap(response -> resp -> resp.onSuccess(accWithRegServerDTO))))
                .onErrorResumeNext(t -> result -> {
                    System.out.println("err: " + t.getMessage());
                    result.onSuccess($Error.mk().createError(t.getMessage()));
                });

    }

    @Override
    public Maybe<Object> setSipServer(SipIgnoreAccDTO sipIgnoreAccDTO, SettingsService settingsService) {
        return Maybe.wrap(settingsService.setSipConfigWithoutAcc(sipIgnoreAccDTO))
                .flatMap(set -> sipClient.updateSipConfig(sipIgnoreAccDTO)
                        .flatMap(res -> resp -> resp.onSuccess(set)))
                .onErrorResumeNext(t -> err -> err.onSuccess($Error.mk().createError(t.getMessage())));
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> createEmergency() {
        return storage.getSettings().toMaybe()
                .flatMap(settings -> networkTools.getIpFromInterface(settings.isVpnNetworkEnabled())
                        .flatMap(networkIPMacDTO ->
                                httpClient.exchange(apiRequests.createEmergency(settings.getCentralServer(), (NetworkIPMacDTO) networkIPMacDTO),
                                                HttpStatus.class)
                                        .firstElement()))
                .onErrorResumeNext(t -> ex -> ex.onSuccess(HttpResponse.ok()));
    }

    @Override
    public Maybe<Object> playEmergency(EmergencyDTO emergencyDTO, WebSocket webSocket) {
        return sipClient.startEmergency()
                .flatMap(resp -> ws -> {
                    webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC,
                            EmergencyDTO.create(emergencyDTO.getUuid(),
                                    emergencyDTO.getEmergencyCategoryName(),
                                    false));
                    webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, emergencyDTO);
                    ws.onSuccess(HttpResponse.ok());
                }).flatMap(resp -> acTools.stop())
                .flatMap(resp -> acTools.play($FileUtils.mk().getAudioPath(emergencyDTO.getUuid())))
                .flatMap(startLight -> gpio -> {
                    GPIO.stopLight();
                    GPIO.startLight();
                    gpio.onSuccess(HttpResponse.ok());
                });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> stopEmergency(EmergencyDTO emergencyDTO, WebSocket webSocket) {
        return sipClient.stopEmergency()
                .flatMap(resp -> ws -> {
                    webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC,
                            EmergencyDTO.create(emergencyDTO.getUuid(),
                                    emergencyDTO.getEmergencyCategoryName(),
                                    false));
                    ws.onSuccess(HttpResponse.ok());
                })
                .flatMap(resp -> acTools.stop())
                .flatMap(stopLight -> gpio -> {
                    GPIO.stopLight();
                    gpio.onSuccess(HttpResponse.ok());
                });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> isOnline() {
        return Maybe.create(resp -> resp.onSuccess(HttpResponse.ok()));
    }
}
