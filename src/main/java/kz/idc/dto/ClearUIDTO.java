package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Setter
@Getter
public class ClearUIDTO {
    private boolean clearUI;

    public static ClearUIDTO create(boolean clearUI){
        ClearUIDTO clearUIDTO = new ClearUIDTO();
        clearUIDTO.setClearUI(clearUI);
        return clearUIDTO;
    }
}
