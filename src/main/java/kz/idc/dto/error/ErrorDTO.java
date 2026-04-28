package kz.idc.dto.error;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class ErrorDTO {
    private final boolean error = true;
    private String description;
}
