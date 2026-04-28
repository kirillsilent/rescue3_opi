package kz.idc.rs.services.client.api.requests;

import io.micronaut.http.HttpRequest;
import kz.idc.dto.DownloadDTO;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.network.NetworkIPMacDTO;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;

public class APIRequestsImpl implements APIRequests {

    private final APIConfiguration apiConfiguration;

    public APIRequestsImpl(APIConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @Override
    public HttpRequest<?> getRegRescueId(ServerAddressDTO serverConfigDTO, NetworkIPMacDTO networkIPMacDTO) {
        return HttpRequest.POST(apiConfiguration.createRegRescuePath(getApiHost(serverConfigDTO)), networkIPMacDTO)
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> createRegSipId(AccWithRegServerDTO accWithRegServerDTO) {
        return HttpRequest.GET(apiConfiguration.createRegSipId(
                getApiSipRegHost(accWithRegServerDTO.getRegServer())));
    }

    @Override
    public HttpRequest<?> updateRegSipId(AccWithRegServerDTO accWithRegServerDTO) {
        return HttpRequest.PUT(apiConfiguration.updateRegSipId(
                getApiSipRegHost(accWithRegServerDTO.getRegServer()), accWithRegServerDTO.getAccount()),
                accWithRegServerDTO.getAccount());
    }

    @Override
    public HttpRequest<?> getAudios(ServerAddressDTO serverConfigDTO) {
        return HttpRequest.GET(apiConfiguration.getAudiosPath(getApiHost(serverConfigDTO)))
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> getPlan(ServerAddressDTO serverConfigDTO, RescueDTO rescueDTO) {
        return HttpRequest.GET(apiConfiguration.getPlanPath(getApiHost(serverConfigDTO), rescueDTO))
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> getMarquees(ServerAddressDTO serverConfigDTO) {
        return HttpRequest.GET(apiConfiguration.getMarquee(getApiHost(serverConfigDTO)))
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> setMarqueeStatus(long id,ServerAddressDTO serverConfigDTO, StatusDTO statusDTO) {
        return HttpRequest.PUT(apiConfiguration.setMarqueeStatus(id, getApiHost(serverConfigDTO)), statusDTO)
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> download(ServerAddressDTO serverConfigDTO, String path, String fileName) {
        return HttpRequest.GET(apiConfiguration.downloadFile(getApiHost(serverConfigDTO), path, fileName))
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> updateDownloadStatusBackend(ServerAddressDTO serverConfigDTO,
                                                      RescueDTO rescueDTO,
                                                      DownloadDTO downloadDTO) {
        return HttpRequest.PUT(apiConfiguration.updateStatusDownloadBackend(getApiHost(serverConfigDTO), rescueDTO), downloadDTO)
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    @Override
    public HttpRequest<?> createEmergency(ServerAddressDTO serverAddressDTO, NetworkIPMacDTO networkIPMacDTO) {
        return HttpRequest.POST(apiConfiguration.createEmergency(getApiHost(serverAddressDTO)), networkIPMacDTO)
                .basicAuth(Auth.USERNAME.VAL, Auth.PASSWORD.VAL);
    }

    private String getApiHost(ServerAddressDTO serverConfigDTO){
        return serverConfigDTO.getHostname() + ":" + serverConfigDTO.getPort();
    }

    private String getApiSipRegHost(ServerAddressDTO sipRegServer){
        return sipRegServer.getHostname() + ":" + sipRegServer.getPort();
    }
}
