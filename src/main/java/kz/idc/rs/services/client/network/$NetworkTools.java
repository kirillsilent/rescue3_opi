package kz.idc.rs.services.client.network;

import io.micronaut.http.client.RxHttpClient;


public class $NetworkTools {

    private $NetworkTools(){}

    private static NetworkToolsImpl mNetworkToolsClientImpl;

    public static NetworkToolsImpl mk() {
        if (mNetworkToolsClientImpl == null) {
            mNetworkToolsClientImpl = new NetworkToolsImpl();
        }
        return mNetworkToolsClientImpl;
    }

}
