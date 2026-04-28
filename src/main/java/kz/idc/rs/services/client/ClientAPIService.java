package kz.idc.rs.services.client;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import kz.idc.rs.services.client.ac.$ACTools;
import kz.idc.rs.services.client.ac.ACTools;
import kz.idc.rs.services.client.api.$APIClient;
import kz.idc.rs.services.client.api.APIClient;
import kz.idc.rs.services.client.api.requests.APIConfiguration;
import kz.idc.rs.services.client.network.$NetworkTools;
import kz.idc.rs.services.client.network.NetworkTools;
import kz.idc.rs.services.client.sip.$SipClient;
import kz.idc.rs.services.client.sip.SipClient;
import kz.idc.rs.services.client.wifi.$WifiTools;
import kz.idc.rs.services.client.wifi.WifiTools;

import javax.inject.Singleton;

@Singleton
public class ClientAPIService {

    public static NetworkTools network(RxHttpClient rxHttpClient) {
        return $NetworkTools.mk();
    }

    public static WifiTools wifi(RxHttpClient rxHttpClient) {
        return $WifiTools.mk();
    }

    public static ACTools ac() {
        return $ACTools.mk();
    }

    public static SipClient sip(RxHttpClient rxHttpClient) {
        return $SipClient.mk(rxHttpClient);
    }

    public static APIClient api(RxHttpClient rxHttpClient,
                                RxStreamingHttpClient rxStreamingHttpClient,
                                APIConfiguration apiConfiguration,
                                NetworkTools networkTools,
                                SipClient sipClient,
                                ACTools acTools) {
        return $APIClient.mk(rxHttpClient,
                rxStreamingHttpClient,
                apiConfiguration,
                networkTools,
                sipClient,
                acTools);
    }

}
