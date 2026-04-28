package kz.idc.rs.services.client.ac;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import kz.idc.dto.VolumeDTO;
import kz.idc.dto.audio.PlayDTO;
import kz.idc.dto.io.IODeviceAvailableDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IODevicesDTO;
import kz.idc.dto.io.IOType;
import kz.idc.error.$Error;
import kz.idc.error.Errors;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ACToolsImpl implements ACTools {
    private static final Logger log = LoggerFactory.getLogger(ACToolsImpl.class);

    private final Storage storage = $Storage.mk();
    private final ACToolsLocalProbe localProbe = new ACToolsLocalProbe();

    public ACToolsImpl() {
    }

    @Override
    public Maybe<Object> getAudioCards(String type) {
        try {
            List<IODeviceDTO> localDevices = localProbe.audioCards(type);
            return Maybe.create(result -> result.onSuccess(localDevices));
        } catch (Exception e) {
            log.warn("Local ac_tools audio probe failed: {}", e.getMessage());
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorACModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<Object> setAudioCard(IODeviceDTO ioDeviceDTO) {
        return storage.setIO(ioDeviceDTO).toMaybe()
                .flatMap(input -> result -> result.onSuccess(input));
    }

    @Override
    public Maybe<Object> getCameras() {
        try {
            List<IODeviceDTO> localDevices = localProbe.cameras();
            return Maybe.create(result -> result.onSuccess(localDevices));
        } catch (Exception e) {
            log.warn("Local ac_tools camera probe failed: {}", e.getMessage());
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorACModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> setVolume(IODeviceDTO ioDeviceDTO, int volume) {
        try {
            localProbe.setVolume(ioDeviceDTO, volume);
            ioDeviceDTO.setVolume(volume);
            storage.setIO(ioDeviceDTO).blockingGet();
            return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
        } catch (Exception e) {
            log.warn("Local ac_tools setVolume failed: {}", e.getMessage());
            return Maybe.create(result -> result.onSuccess(HttpResponse.serverError()));
        }
    }

    @Override
    public Maybe<Object> getVolume(IODeviceDTO ioDeviceDTO) {
        try {
            VolumeDTO localVolume = localProbe.getVolume(ioDeviceDTO);
            return Maybe.create(result -> result.onSuccess(localVolume));
        } catch (Exception e) {
            log.warn("Local ac_tools getVolume failed: {}", e.getMessage());
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorACModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<Object> isAvailable(String type) {
        Maybe<IODeviceDTO> maybe = storage.getIO(type).toMaybe();
        return maybe.flatMap(io -> Maybe.create(check -> {
            if (io.getDevice() != null) {
                check.onSuccess(io);
            } else {
                check.onError(new Throwable(Errors.NULL_POINTER_AC_DEVICE.EXCEPTION));
            }
        }).flatMap(d -> createRequestAvailable(type, io))
                .flatMap(ioDevicesDTO -> Maybe.create(check -> {
                    List<IODeviceDTO> devices = ioDevicesDTO.getDevices();
                    IODeviceAvailableDTO ioDeviceAvailableDTO;
                    if (devices.isEmpty()) {
                        ioDeviceAvailableDTO = IODeviceAvailableDTO.create(type, false);
                    } else {
                        ioDeviceAvailableDTO = IODeviceAvailableDTO.create(type, devices.get(0).getDevice().equals(io.getDevice()));
                    }
                    check.onSuccess(ioDeviceAvailableDTO);
                }))).onErrorResumeNext(t -> result -> {
            if (t.getMessage().equals(Errors.NULL_POINTER_AC_DEVICE.EXCEPTION)) {
                result.onSuccess(IODeviceAvailableDTO.create(type, false));
            } else {
                result.onSuccess($Error.mk().createErrorACModule(t.getMessage()));
            }
        });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> play(String track) {
        return storage.getIO(IOType.AUDIO_OUTPUT.DEVICE).toMaybe()
                .flatMap(io -> {
                    try {
                        localProbe.play(io, track);
                        return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
                    } catch (Exception e) {
                        log.warn("Local ac_tools play failed: {}", e.getMessage());
                        return Maybe.create(result -> result.onSuccess(HttpResponse.serverError()));
                    }
                });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> stop() {
        try {
            localProbe.stopPlayback();
            return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
        } catch (Exception e) {
            log.warn("Local ac_tools stop failed: {}", e.getMessage());
            return Maybe.create(result -> result.onSuccess(HttpResponse.serverError()));
        }
    }

    private Maybe<IODevicesDTO> createRequestAvailable(String type, IODeviceDTO io) {
        try {
            IODeviceAvailableDTO local = localProbe.isAvailable(type, io);
            IODevicesDTO dto = new IODevicesDTO();
            List<IODeviceDTO> devices = new java.util.ArrayList<>();
            if (local.isAvailable()) {
                devices.add(io);
            }
            dto.setDevices(devices);
            return Maybe.create(result -> result.onSuccess(dto));
        } catch (Exception e) {
            return Maybe.error(e);
        }
    }
}
