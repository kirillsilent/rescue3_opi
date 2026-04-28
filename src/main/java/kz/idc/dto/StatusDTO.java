package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class StatusDTO {
    private boolean status;

    public static StatusDTO create(boolean status){
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatus(status);
        return statusDTO;
    }
}
