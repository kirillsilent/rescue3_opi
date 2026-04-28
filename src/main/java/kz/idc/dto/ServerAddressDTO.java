package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class ServerAddressDTO {

    private String hostname;
    private int port;

    public static ServerAddressDTO create(String hostname, int port){
        ServerAddressDTO serverAddressDTO = new ServerAddressDTO();
        serverAddressDTO.setHostname(hostname);
        serverAddressDTO.setPort(port);
        return serverAddressDTO;
    }
}
