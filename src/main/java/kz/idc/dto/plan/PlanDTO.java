package kz.idc.dto.plan;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.PathDTO;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class PlanDTO extends PathDTO {
    private String name;
    private String checksum;
}
