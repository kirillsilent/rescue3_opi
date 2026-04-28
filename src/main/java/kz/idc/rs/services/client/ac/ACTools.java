package kz.idc.rs.services.client.ac;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import kz.idc.dto.io.IODeviceDTO;

public interface ACTools {
    Maybe<Object> getAudioCards(String type);
    Maybe<Object> setAudioCard(IODeviceDTO ioDeviceDTO);
    Maybe<Object> getCameras();
    Maybe<HttpResponse<HttpStatus>> setVolume(IODeviceDTO ioDeviceDTO, int volume);
    Maybe<Object> getVolume(IODeviceDTO ioDeviceDTO);
    Maybe<Object> isAvailable(String type);
    Maybe<HttpResponse<HttpStatus>> play(String track);
    Maybe<HttpResponse<HttpStatus>> stop();
}
