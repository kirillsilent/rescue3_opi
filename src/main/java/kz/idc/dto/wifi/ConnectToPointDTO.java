package kz.idc.dto.wifi;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Introspected
@EqualsAndHashCode(callSuper = true)
@Data
public class ConnectToPointDTO extends PointDTO {
    private String device;
    private String psk;

    public static ConnectToPointDTO update(ConnectToPointDTO connectToPointDTO, String device){
        connectToPointDTO.setDevice(device);
        return connectToPointDTO;
    }

}
