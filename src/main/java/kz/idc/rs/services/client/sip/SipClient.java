package kz.idc.rs.services.client.sip;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.ws.WebSocket;


public interface SipClient {
    Maybe<StatusDTO> getSipStatus();
    Maybe<Object> restart();
    Maybe<Object> stop();
    Maybe<HttpResponse<HttpStatus>> startEmergency();
    Maybe<HttpResponse<HttpStatus>> stopEmergency();
    Maybe<Object> incomingCall(WebSocket webSocket);
    Maybe<HttpResponse<HttpStatus>> call();
    Maybe<IODeviceDTO> updateIO(IODeviceDTO ioDeviceDTO);
    Maybe<Object> end(WebSocket webSocket);
    Maybe<HttpResponse<HttpStatus>> updateSipAccount(AccWithRegServerDTO accWithRegServerDTO);
    Maybe<HttpResponse<HttpStatus>> updateSipConfig(SipIgnoreAccDTO sipIgnoreAccDTO);
    default void bindWebSocket(WebSocket webSocket) {}
}
