package kz.idc.utils.downloader;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import kz.idc.rs.services.client.api.requests.APIRequests;
import kz.idc.ws.WebSocket;

import javax.inject.Singleton;

@Singleton
public class $Downloader {
    private static Download download;

    public static Download mk(RxHttpClient rxHttpClient, RxStreamingHttpClient rxStreamingHttpClient, APIRequests apiRequests){
        if (download == null){
            download = new DownloadImpl(rxHttpClient, rxStreamingHttpClient, apiRequests);
        }
        return download;
    }
}
