package kz.idc.dto.io;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class IODeviceAvailableDTO {
    private boolean available;
    private String type;

    public static IODeviceAvailableDTO create(String type, boolean available){
        IODeviceAvailableDTO ioDeviceAvailableDTO = new IODeviceAvailableDTO();
        ioDeviceAvailableDTO.setType(type);
        ioDeviceAvailableDTO.setAvailable(available);
        return ioDeviceAvailableDTO;
    }
}
