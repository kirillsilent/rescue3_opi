package kz.idc.dto.sip.acc;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class PasswordDTO {
    private String password;
}
