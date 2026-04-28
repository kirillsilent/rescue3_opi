package kz.idc.rs.services.client.api.requests;

import io.micronaut.http.HttpRequest;
import kz.idc.dto.DownloadDTO;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.network.NetworkIPMacDTO;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;

public interface APIRequests {
    HttpRequest<?> getRegRescueId(ServerAddressDTO serverConfigDTO, NetworkIPMacDTO networkIPMacDTO);
    HttpRequest<?> createRegSipId(AccWithRegServerDTO accWithRegServerDTO);
    HttpRequest<?> updateRegSipId(AccWithRegServerDTO accWithRegServerDTO);
    HttpRequest<?> getAudios(ServerAddressDTO serverConfigDTO);
    HttpRequest<?> getPlan(ServerAddressDTO serverConfigDTO, RescueDTO rescueDTO);
    HttpRequest<?> getMarquees(ServerAddressDTO serverConfigDTO);
    HttpRequest<?> setMarqueeStatus(long id, ServerAddressDTO serverConfigDTO, StatusDTO statusDTO);
    HttpRequest<?> download(ServerAddressDTO serverConfigDTO, String path, String fileName);
    HttpRequest<?> updateDownloadStatusBackend(ServerAddressDTO serverConfigDTO, RescueDTO rescueDTO, DownloadDTO downloadDTO);
    HttpRequest<?> createEmergency(ServerAddressDTO serverAddressDTO, NetworkIPMacDTO networkIPMacDTO);
}
