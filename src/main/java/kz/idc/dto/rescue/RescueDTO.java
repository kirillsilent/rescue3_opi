package kz.idc.dto.rescue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RescueDTO {
    private long id;

    public static RescueDTO create(long id){
        RescueDTO rescueDTO = new RescueDTO();
        rescueDTO.setId(id);
        return rescueDTO;
    }
}
