package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class IsSettingsSetDTO {
    private boolean set;

    public static IsSettingsSetDTO create (boolean set){
        IsSettingsSetDTO isSettingsSetDTO = new IsSettingsSetDTO();
        isSettingsSetDTO.setSet(set);
        return isSettingsSetDTO;
    }
}
