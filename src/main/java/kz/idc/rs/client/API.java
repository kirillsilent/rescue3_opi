package kz.idc.rs.client;

import io.micronaut.http.client.RxHttpClient;
import kz.idc.rs.client.card.$ApiCard;
import kz.idc.rs.client.card.ApiCardImpl;
import kz.idc.rs.client.card.requests.ApiCardConfig;
import kz.idc.rs.client.sip.$ApiSip;
import kz.idc.rs.client.sip.ApiSip;


public class API {

    public static ApiSip sip(RxHttpClient rxHttpClient) {
        return $ApiSip.mk(rxHttpClient);
    }

    public static ApiCardImpl card(RxHttpClient rxHttpClient, ApiCardConfig apiCardConfig) {
        return $ApiCard.mk(rxHttpClient, apiCardConfig);
    }
}
