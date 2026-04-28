package kz.idc.rs.services.client.sip;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.dto.ClearUIDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.sip.IncomingDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.utils.gpio.GPIO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import kz.idc.ws.WebSocket;
import kz.idc.ws.WebSocketTopics;


public class SipClientImpl implements SipClient{
    private final Storage storage = $Storage.mk();
    private final SipClientLocalProbe localProbe = new SipClientLocalProbe();
    private volatile WebSocket webSocket;
    private final SipClientLinphonecRuntime runtime;

    public SipClientImpl(RxHttpClient rxHttpClient){
        this.runtime = new SipClientLinphonecRuntime(new SipClientLinphonecRuntime.Listener() {
            @Override
            public void onRegistrationChanged(boolean ok) {
                if (!ok) {
                    // keep behavior minimal; UI polls status and will see false
                    return;
                }
            }

            @Override
            public void onIncomingCall() {
                WebSocket ws = webSocket;
                if (ws != null) {
                    incomingCall(ws).subscribe();
                }
            }

            @Override
            public void onCallEnded() {
                WebSocket ws = webSocket;
                if (ws != null) {
                    end(ws).subscribe();
                }
            }

            @Override
            public void onCallConnected() {
                // no-op; UI callback for "incoming" is not emitted by linphonec polling backend
            }

            @Override
            public void onError(String message) {
                // status polling will expose error; avoid recursive restart loops here
            }
        });
        this.runtime.ensureStarted();
    }

    @Override
    public Maybe<StatusDTO> getSipStatus() {
        return Maybe.just(runtime.status())
                .onErrorReturnItem(localProbe.status());
    }

    @Override
    public Maybe<Object> restart() {
        return Maybe.create(s -> {
                    runtime.restart();
                    s.onSuccess(HttpResponse.ok());
                }
        );
    }

    @Override
    public Maybe<Object> stop() {
        return Maybe.create(s -> {
            runtime.shutdown();
            s.onSuccess(HttpResponse.ok());
        });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> startEmergency() {
        clearLight();
        return Maybe.create(s -> {
            runtime.startEmergency();
            s.onSuccess(HttpResponse.ok());
        });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> stopEmergency() {
        return Maybe.create(s -> {
            runtime.stopEmergency();
            s.onSuccess(HttpResponse.ok());
        });
    }

    @Override
    public Maybe<Object> incomingCall(WebSocket webSocket) {
        return Maybe.create(s -> {
            GPIO.startLight();
            webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, IncomingDTO.create(true));
            s.onSuccess(HttpResponse.ok());
        });
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> call() {
        return Maybe.fromCallable(() -> {
            runtime.call();
            return (HttpResponse<HttpStatus>) HttpResponse.ok(HttpStatus.OK);
        }).onErrorReturnItem((HttpResponse<HttpStatus>) HttpResponse.serverError(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    public Maybe<IODeviceDTO> updateIO(IODeviceDTO ioDeviceDTO) {
        return storage.setIO(ioDeviceDTO).toMaybe()
                .flatMap(io -> Maybe.create(result -> {
                    try {
                        runtime.updateIO();
                    } catch (Exception ignored) {
                    }
                    result.onSuccess(ioDeviceDTO);
                }));
    }

    @Override
    public Maybe<Object> end(WebSocket webSocket) {
       return Maybe.create(s -> {
            clearLight();
            webSocket.onMessage(WebSocketTopics.RESCUE.TOPIC, ClearUIDTO.create(true));
            s.onSuccess(HttpResponse.ok());
        });
    }

    private void clearLight(){
        GPIO.stopLight();
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> updateSipAccount(AccWithRegServerDTO accWithRegServerDTO) {
        return Maybe.fromCallable(() -> {
            if (accWithRegServerDTO != null && accWithRegServerDTO.getAccount() != null) {
                storage.setSipAccount(accWithRegServerDTO.getAccount()).blockingGet();
            }
            runtime.updateSipAccount();
            return (HttpResponse<HttpStatus>) HttpResponse.ok(HttpStatus.OK);
        }).onErrorReturnItem((HttpResponse<HttpStatus>) HttpResponse.serverError(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> updateSipConfig(SipIgnoreAccDTO sipIgnoreAccDTO) {
        return Maybe.fromCallable(() -> {
            if (sipIgnoreAccDTO != null) {
                storage.setSipConfig(sipIgnoreAccDTO).blockingGet();
            }
            runtime.updateSipConfig();
            return (HttpResponse<HttpStatus>) HttpResponse.ok(HttpStatus.OK);
        }).onErrorReturnItem((HttpResponse<HttpStatus>) HttpResponse.serverError(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    public void bindWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
