package kz.idc.rs;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.Maybe;
import io.reactivex.Single;
import kz.idc.dto.EmergencyDTO;
import kz.idc.dto.sip.acc.AccountDTO;
import kz.idc.rs.client.card.$ApiCard;
import kz.idc.rs.client.card.ApiCardImpl;
import kz.idc.rs.client.card.requests.ApiCardConfig;
import kz.idc.rs.client.sip.$ApiSip;
import kz.idc.rs.client.sip.ApiSip;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.ac.ACTools;
import kz.idc.rs.services.client.api.APIClient;
import kz.idc.rs.services.client.api.requests.APIConfiguration;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import kz.idc.ws.WebSocket;
import kz.idc.ws.WebSocketTopics;

@Controller("/api")
public class API {

    private final APIClient apiClient;
    private final Storage storage = $Storage.mk();
    private final WebSocket webSocket;

    public API(RxHttpClient rxHttpClient, RxStreamingHttpClient rxStreamingHttpClient,
               APIConfiguration apiConfiguration,
               WebSocket webSocket){
        this.webSocket = webSocket;
        ACTools acTools = ClientAPIService.ac();
        apiClient = ClientAPIService.api(rxHttpClient,
                rxStreamingHttpClient,
                apiConfiguration,
                ClientAPIService.network(rxHttpClient),
                ClientAPIService.sip(rxHttpClient),
                acTools);
    }

    @Get("/get_sip_account")
    public Single<AccountDTO> getSipAccount() {
        return storage.getSipAccount();
    }

    @Get("/download_plan")
    public Maybe<Object> downloadPlan() {
        return apiClient.getPlan(WebSocketTopics.RESCUE.TOPIC,
                webSocket);
    }

    @Get("/download_audios")
    public Maybe<Object> downloadAudios() {
        return apiClient.getAudios(WebSocketTopics.RESCUE.TOPIC, webSocket);
    }

    @Get("/set_marquee")
    public Maybe<Object> downloadMarquee() {
        return apiClient.getMarquees(webSocket);
    }

    @Post("/emergency")
    public Maybe<Object> setEmergency(EmergencyDTO emergencyDTO) {
        return apiClient.playEmergency(emergencyDTO, webSocket);
    }

    @Put("/emergency")
    public Maybe<HttpResponse<HttpStatus>> updateEmergency(EmergencyDTO emergencyDTO) {
        return apiClient.stopEmergency(emergencyDTO, webSocket);
    }

    @Get("/is_online")
    public Maybe<HttpResponse<HttpStatus>> isOnline(){
        return apiClient.isOnline();
    }
        public static ApiSip sip(RxHttpClient rxHttpClient) {
        return $ApiSip.mk(rxHttpClient);
    }

    public static ApiCardImpl card(RxHttpClient rxHttpClient, ApiCardConfig apiCardConfig) {
        return $ApiCard.mk(rxHttpClient, apiCardConfig);
    }
}
