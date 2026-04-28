package kz.idc.rs.services.client.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import kz.idc.dto.EmergencyDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.rs.services.settings.SettingsService;
import kz.idc.ws.WebSocket;

public interface APIClient {
    Maybe<Object> getRegRescueId(ServerAddressDTO serverConfigDTO);
    Maybe<Object> getAudios(String uuid, WebSocket webSocket);
    Maybe<Object> getPlan(String uuid, WebSocket webSocket);
    Maybe<Object> getMarquees(WebSocket webSocket);
    Maybe<Object> getRegSipAccount(AccWithRegServerDTO sipAccWithRegServerDTO);
    Maybe<Object> setSipServer(SipIgnoreAccDTO sipIgnoreAccDTO, SettingsService settingsService);
    Maybe<HttpResponse<HttpStatus>> createEmergency();
    Maybe<Object> playEmergency(EmergencyDTO emergencyDTO, WebSocket webSocket);
    Maybe<HttpResponse<HttpStatus>> stopEmergency(EmergencyDTO emergencyDTO, WebSocket webSocket);
    Maybe<HttpResponse<HttpStatus>> isOnline();
}
