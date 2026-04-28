package kz.idc.dto.wifi;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Introspected
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class PointIsConnectedDTO extends PointDTO {
    private String device;

    public static PointIsConnectedDTO create(String ssid, String device){
        PointIsConnectedDTO pointIsConnectedDTO = new PointIsConnectedDTO();
        pointIsConnectedDTO.setSsid(ssid);
        pointIsConnectedDTO.setDevice(device);
        return pointIsConnectedDTO;
    }
}
