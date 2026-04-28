package kz.idc.dto.sip.acc;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class AccountDTO extends PasswordDTO {
    private String account;

    public static AccountDTO create(String account, String password){
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAccount(account);
        accountDTO.setPassword(password);
        return accountDTO;
    }
}
