package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Setter
@Getter
public class SipDTO {
    private String callStatus;
}
