package kz.idc.dto.io;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class IODeviceDTO {
    private String device;
    private String type;
    private Integer volume;
}
