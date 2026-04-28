package kz.idc.rs.services.client.api;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import kz.idc.rs.services.client.ac.ACTools;
import kz.idc.rs.services.client.network.NetworkTools;
import kz.idc.rs.services.client.sip.SipClient;
import kz.idc.rs.services.client.api.requests.APIConfiguration;


public class $APIClient {

    private $APIClient(){}

    private static APIClient mAPIClientImpl;

    public static APIClient mk(RxHttpClient rxHttpClient,
                               RxStreamingHttpClient rxStreamingHttpClient,
                               APIConfiguration configuration,
                               NetworkTools networkTools,
                               SipClient sipClient,
                               ACTools acTools) {
        if (mAPIClientImpl == null) {
            mAPIClientImpl = new APIClientImpl(rxHttpClient,
                    rxStreamingHttpClient,
                    configuration,
                    networkTools,
                    sipClient,
                    acTools);
        }
        return mAPIClientImpl;
    }
}
