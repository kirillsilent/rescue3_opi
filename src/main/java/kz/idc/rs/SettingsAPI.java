package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.reactivex.Maybe;
import kz.idc.Application;
import kz.idc.dto.settings.ApiDTO;
import kz.idc.dto.settings.EventServerDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.api.APIClient;
import kz.idc.rs.services.client.api.requests.APIConfiguration;
import kz.idc.rs.services.settings.$SettingsService;
import kz.idc.rs.services.settings.SettingsService;
import kz.idc.ws.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/settings_api")
public class SettingsAPI {

    private static final Logger log = LoggerFactory.getLogger(SettingsAPI.class);
    private final APIClient apiClient;
    private final SettingsService settingsService = $SettingsService.mk();
    private final WebSocket webSocket;

    public SettingsAPI(RxHttpClient rxHttpClient, RxStreamingHttpClient rxStreamingHttpClient,
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
    public Maybe<ApiDTO> getApiSettings() {
        return settingsService.getAllApiSettings();

    }

    @Put("/get_reg_rescue")
    public Maybe<Object> getRescueId(ServerAddressDTO serverConfigDTO) {
        return apiClient.getRegRescueId(serverConfigDTO);
    }

    @Put("/get_reg_sip")
    public Maybe<Object> getSipAccount(AccWithRegServerDTO accWithRegServerDTO) {
        return apiClient.getRegSipAccount(accWithRegServerDTO);
    }

    @Get("/get_audios")
    public Maybe<Object> getAudios(@QueryValue String uuid) {
        return apiClient.getAudios(uuid, webSocket);
    }

    @Get("/get_plan")
    public Maybe<Object> getPlan(@QueryValue String uuid) {
        return apiClient.getPlan(uuid, webSocket);
    }

    @Put("/set_sip_config")
    public Maybe<Object> setSipServer(SipIgnoreAccDTO sipIgnoreAccDTO) {
        return apiClient.setSipServer(sipIgnoreAccDTO, settingsService);
    }

    @Put("/set_event_server")
    public Maybe<EventServerDTO> setEventServer(EventServerDTO eventServerDTO) {
        return settingsService.setEventServerUrl(eventServerDTO);
    }
}
