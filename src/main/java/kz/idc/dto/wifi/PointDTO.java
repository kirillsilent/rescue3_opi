package kz.idc.dto.wifi;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class PointDTO {
    private String ssid;
}
