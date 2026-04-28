package kz.idc.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import io.reactivex.Maybe;
import kz.idc.dto.ReloadDTO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import kz.idc.ws.WebSocket;
import kz.idc.ws.WebSocketTopics;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class ViewController {
    private final WebSocket webSocket;
    private final Storage storage = $Storage.mk();

    @Get()
    public ModelAndView<Object>root() {
        if(storage.isSettingsSetBool()){
            webSocket.onMessage(WebSocketTopics.WELCOME.TOPIC, ReloadDTO.create(true));
            return new ModelAndView<>("rescue", null);
        }else {
            return new ModelAndView<>("index", null);
        }
    }

    @View("settings")
    @Get("/settings")
    public Maybe<HttpResponse<HttpStatus>> settings() {
        return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
    }

    @View("settings_login")
    @Get("/settings/login")
    public Maybe<HttpResponse<HttpStatus>> settingsLogin() {
        return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
    }

}
