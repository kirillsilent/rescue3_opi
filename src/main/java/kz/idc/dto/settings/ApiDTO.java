package kz.idc.dto.settings;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipDTO;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class ApiDTO {
    private RescueDTO rescueId;
    private ServerAddressDTO centralServer;
    private SipDTO sip;
    private String eventServerUrl;
    private String deviceSn;

    public static ApiDTO create (RescueDTO rescueDTO,
                                 ServerAddressDTO centralServer,
                                 SipDTO sipDTO,
                                 String eventServerUrl,
                                 String deviceSn){
        ApiDTO apiDTO = new ApiDTO();
        apiDTO.setRescueId(rescueDTO);
        apiDTO.setCentralServer(centralServer);
        apiDTO.setSip(sipDTO);
        apiDTO.setEventServerUrl(eventServerUrl);
        apiDTO.setDeviceSn(deviceSn);
        return apiDTO;
    }
}
