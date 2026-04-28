package kz.idc.dto.network;


import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Introspected
@Getter
@Setter
public class NetworkDevicesDTO {
    private String type;
    private List<String> interfaces;

    public static NetworkDevicesDTO create(String type, List<String> devices){
        NetworkDevicesDTO networkDevicesDTO = new NetworkDevicesDTO();
        networkDevicesDTO.setType(type);
        networkDevicesDTO.setInterfaces(devices);
        return networkDevicesDTO;
    }

}
