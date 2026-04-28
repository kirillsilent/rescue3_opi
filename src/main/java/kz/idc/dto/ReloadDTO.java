package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class ReloadDTO {
    private boolean reload;

    public static ReloadDTO create(boolean reload){
        ReloadDTO reloadDTO = new ReloadDTO();
        reloadDTO.setReload(reload);
        return reloadDTO;
    }
}
