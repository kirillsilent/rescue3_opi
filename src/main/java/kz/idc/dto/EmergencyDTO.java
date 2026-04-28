package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class EmergencyDTO {
    private String uuid;
    private String emergencyCategoryName;
    private boolean isActive;

    public static EmergencyDTO create(String uuid, String emergencyCategoryName, boolean isActive){
        EmergencyDTO emergencyDTO = new EmergencyDTO();
        emergencyDTO.setUuid(uuid);
        emergencyDTO.setEmergencyCategoryName(emergencyCategoryName);
        emergencyDTO.setActive(isActive);
        return emergencyDTO;
    }
}
