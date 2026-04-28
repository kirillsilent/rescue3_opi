package kz.idc.rs;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.dto.StatusDTO;
import kz.idc.rs.services.client.sip.$SipClient;
import kz.idc.rs.services.client.sip.SipClient;
import kz.idc.ws.WebSocket;

@Controller("/sip_client")
public class SipClientApi {

    private final SipClient sipClient;
    private final WebSocket webSocket;

    public SipClientApi(RxHttpClient rxHttpClient, WebSocket webSocket){
        this.webSocket = webSocket;
        sipClient = $SipClient.mk(rxHttpClient);
        sipClient.bindWebSocket(webSocket);
    }

    @Get("/status")
    public Maybe<StatusDTO> getSipStatus(){
        return sipClient.getSipStatus();
    }

    @Get("/stop")
    public Maybe<Object> stop(){
        return sipClient.stop();
    }

    @Get("/restart")
    public Maybe<Object> restart(){
        return sipClient.restart();
    }

    @Get("/call")
    public Maybe<HttpResponse<HttpStatus>> call(){
        return sipClient.call();
    }

    @Get("/incoming_call")
    public Maybe<Object> incomingCall(){
        return sipClient.incomingCall(webSocket);
    }

    @Get("/end")
    public Maybe<Object> endCall(){
        return sipClient.end(webSocket);
    }

    @Get("/sip_error")
    public Maybe<Object> sipError(){
        return sipClient.restart();
    }

}
