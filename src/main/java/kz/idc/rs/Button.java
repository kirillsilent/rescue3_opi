package kz.idc.rs;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.rs.client.API;
import kz.idc.rs.client.card.ApiCard;
import kz.idc.rs.client.card.requests.ApiCardConfig;
import kz.idc.rs.client.sip.ApiSip;
import kz.idc.utils.gpio.GPIO;


@Controller
public class Button {

    private final ApiSip apiSip;
    private final ApiCard apiCard;
    private final static String SIP_CLIENT_PATH = "/sip_client/";

    public Button(RxHttpClient rxHttpClient,
                  ApiCardConfig apiCardConfig){
        apiSip = API.sip(rxHttpClient);
        apiCard = API.card(rxHttpClient, apiCardConfig);
    }

    @Get(SIP_CLIENT_PATH + "sip_started")
    public void started() {
        apiSip.started();
    }

    @Get("/call")
    public Maybe<HttpResponse<HttpStatus>> call() {
        return apiSip.call();
    }

    @Get("/card")
    public Maybe<Object> card() {
        return apiCard.post();
    }

    @Get(SIP_CLIENT_PATH + "end")
    public void end() {
        GPIO.stopLight();
        apiSip.end();
        apiCard.unlockPostCard();
    }

    @Get(SIP_CLIENT_PATH + "sip_error")
    public void sip_error() {
        GPIO.stopLight();
        apiCard.unlockPostCard();
        apiSip.error();
    }
}
