package kz.idc.dto.wifi;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Introspected
@EqualsAndHashCode(callSuper = true)
@Data
public class AdvancedPointDTO extends PointDTO {
    private int signal;
    private boolean encrypted;
}
