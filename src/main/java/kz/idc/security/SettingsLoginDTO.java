package kz.idc.security;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class SettingsLoginDTO {
    private String password;
}

