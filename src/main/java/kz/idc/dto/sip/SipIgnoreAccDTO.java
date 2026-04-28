package kz.idc.dto.sip;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.ServerAddressDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Introspected
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class SipIgnoreAccDTO extends ServerAddressDTO {
    private String operator;
}
