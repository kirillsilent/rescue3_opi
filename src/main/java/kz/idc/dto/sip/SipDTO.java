package kz.idc.dto.sip;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.acc.AccountDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Introspected
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class SipDTO extends ServerAddressDTO {
    private String operator;
    private AccountDTO account;
    private ServerAddressDTO sipRegServer;
}
