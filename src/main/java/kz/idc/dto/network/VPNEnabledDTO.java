package kz.idc.dto.network;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VPNEnabledDTO {
    private boolean vpnNetworkEnabled;

    public static VPNEnabledDTO create(boolean enabled){
        VPNEnabledDTO vpnEnabledDTO = new VPNEnabledDTO();
        vpnEnabledDTO.setVpnNetworkEnabled(enabled);
        return vpnEnabledDTO;
    }
}
