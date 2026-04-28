package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.network.NetworkConfigDTO;
import kz.idc.dto.network.VPNEnabledDTO;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.network.NetworkTools;


@Controller("/network")
public class Network {

    private final NetworkTools networkTools;

    public Network(RxHttpClient rxHttpClient) {
        this.networkTools = ClientAPIService.network(rxHttpClient);
    }

    @Put("/set_interface")
    public Maybe<Object>setDefaultNetworkInterface(IODeviceDTO ioDeviceDTO) {
        return networkTools.setNetworkInterface(ioDeviceDTO);
    }

    @Put("/set_config")
    public Maybe<Object> setNetworkConfig(NetworkConfigDTO networkConfigDTO){
        return networkTools.setNetworkConfig(networkConfigDTO);
    }

    @Get("/get_interfaces")
    public Maybe<Object> getNetworkInterfaces(@QueryValue String type) {
        return networkTools.getNetworkInterfaces(type);
    }

    @Get("/get_default_route")
    public Maybe<Object>getDefaultRoute() {
        return networkTools.isDefaultRoute();
    }

    @Get("/get_current_interface")
    public Maybe<Object>getDefaultNetworkInterface() {
        return networkTools.getCurrentNetworkInterface();
    }

    @Get("/status")
    public Maybe<Object>getStatus() {
        return networkTools.getStatusNetworkInterface();
    }

    @Put("/add_default_route")
    public Maybe<Object>addDefaultRoute() {
        return networkTools.addDefaultRoute();
    }

    @Put("/set_work_on_vpn")
    public Maybe<VPNEnabledDTO>setWorkOnVPN(VPNEnabledDTO vpnEnabledDTO) {
        return networkTools.setWorkOnVPN(vpnEnabledDTO);
    }

    @Get("/get_work_on_vpn")
    public Maybe<VPNEnabledDTO>getWorkOnVPN() {
        return networkTools.getStateWorkOnVPN();
    }
}
