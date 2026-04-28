package kz.idc.rs.services.client.sip;

import io.micronaut.http.client.RxHttpClient;

public class $SipClient {

    private static SipClient sipClient;

    public static SipClient mk(RxHttpClient rxHttpClient){
        if(sipClient == null){
            sipClient = new SipClientImpl(rxHttpClient);
        }
        return sipClient;
    }

}
