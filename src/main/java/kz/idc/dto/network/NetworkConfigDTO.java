package kz.idc.dto.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkConfigDTO {
    private String iface;
    private String ip;
    private String netmask;
    private String gateway;
    private boolean staticIp;
    private String vpn;
    private String dns1;
    private String dns2;
}
