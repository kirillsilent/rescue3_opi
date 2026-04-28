package kz.idc.rs.services.client.network;

import io.reactivex.Maybe;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.network.NetworkConfigDTO;
import kz.idc.dto.network.VPNEnabledDTO;


public interface NetworkTools {
    Maybe<Object> getNetworkInterfaces(String type);
    Maybe<Object> setNetworkConfig(NetworkConfigDTO networkConfigDTO);
    Maybe<Object> setNetworkInterface(IODeviceDTO iface);
    Maybe<Object> getCurrentNetworkInterface();
    Maybe<Object> getStatusNetworkInterface();
    Maybe<Object> isDefaultRoute();
    Maybe<Object> addDefaultRoute();
    Maybe<Object> getIpFromInterface(boolean isVPNEnabled);
    Maybe<VPNEnabledDTO> setWorkOnVPN(VPNEnabledDTO vpnEnabledDTO);
    Maybe<VPNEnabledDTO> getStateWorkOnVPN();
}
