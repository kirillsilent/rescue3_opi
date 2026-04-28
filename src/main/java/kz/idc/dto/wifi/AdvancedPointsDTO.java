package kz.idc.dto.wifi;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Introspected
@Getter
@Setter
public class AdvancedPointsDTO {
    private List<AdvancedPointDTO> points;
}
