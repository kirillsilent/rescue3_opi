package kz.idc.dto.sip.acc;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.ServerAddressDTO;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class AccWithRegServerDTO {

    private AccountDTO account;
    private ServerAddressDTO regServer;

    public static AccWithRegServerDTO create(ServerAddressDTO address, AccountDTO account) {
        AccWithRegServerDTO accWithRegServerDTO = new AccWithRegServerDTO();
        accWithRegServerDTO.setAccount(AccountDTO.create(account.getAccount(), account.getPassword()));
        accWithRegServerDTO.setRegServer(ServerAddressDTO.create(address.getHostname(), address.getPort()));
        return accWithRegServerDTO;
    }
}
