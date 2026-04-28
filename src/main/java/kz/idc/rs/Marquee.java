package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.Maybe;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.api.APIClient;
import kz.idc.rs.services.client.api.requests.APIConfiguration;
import kz.idc.ws.WebSocket;

@Controller("/marquee")
public class Marquee {
    private final APIClient apiClient;
    private final WebSocket webSocket;

    public Marquee(RxHttpClient rxHttpClient, RxStreamingHttpClient rxStreamingHttpClient,
                   APIConfiguration apiConfiguration,
                   WebSocket webSocket) {
        this.webSocket = webSocket;
        apiClient = ClientAPIService.api(rxHttpClient,
                rxStreamingHttpClient,
                apiConfiguration,
                ClientAPIService.network(rxHttpClient),
                ClientAPIService.sip(rxHttpClient),
                ClientAPIService.ac());
    }


    @Get("/get")
    public Maybe<Object> getMarquee(){
        return apiClient.getMarquees(webSocket);
    }
}
