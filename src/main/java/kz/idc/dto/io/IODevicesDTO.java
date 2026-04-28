package kz.idc.dto.io;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Introspected
@Getter
@Setter
public class IODevicesDTO {
    private List<IODeviceDTO> devices;
}
