package kz.idc.dto.settings;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Introspected
@Getter
@Setter
public class SettingsDTO {
    private RescueDTO rescue;
    private boolean vpnNetworkEnabled;
    private ServerAddressDTO centralServer;
    private SipDTO sip;
    private List <IODeviceDTO> io;
    private String eventServerUrl;
    private String deviceSn;
}
