package kz.idc.dto.network;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class NetworkIPDTO {
    private String ip;
}
