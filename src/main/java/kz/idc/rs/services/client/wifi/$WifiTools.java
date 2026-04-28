package kz.idc.rs.services.client.wifi;

import io.micronaut.http.client.RxHttpClient;


public class $WifiTools {

    private $WifiTools(){}

    private static WifiToolsImpl mWifiToolsClientImpl;

    public static WifiToolsImpl mk() {
        if (mWifiToolsClientImpl == null) {
            mWifiToolsClientImpl = new WifiToolsImpl();
        }
        return mWifiToolsClientImpl;
    }

}
