package kz.idc.rs.client.card;

import io.micronaut.http.client.RxHttpClient;
import kz.idc.rs.client.card.requests.ApiCardConfig;

public class $ApiCard {
    private static ApiCardImpl apiCard;

    public static ApiCardImpl mk(RxHttpClient rxHttpClient, ApiCardConfig apiCardConfig) {
        if (apiCard == null) {
            apiCard = new ApiCardImpl(rxHttpClient, apiCardConfig);
        }
        return apiCard;
    }
}
