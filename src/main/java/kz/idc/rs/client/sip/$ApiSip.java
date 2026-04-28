package kz.idc.rs.client.sip;

import io.micronaut.http.client.RxHttpClient;

public class $ApiSip {

    private static ApiSip apiSip;

    public static ApiSip mk(RxHttpClient rxHttpClient){
        if(apiSip == null){
            apiSip = new ApiSipImpl(rxHttpClient);
        }
        return apiSip;
    }
}
