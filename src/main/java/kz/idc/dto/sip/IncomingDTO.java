package kz.idc.dto.sip;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Setter
@Getter
public class IncomingDTO {
    private boolean incoming;

    public static IncomingDTO create(boolean incoming){
        IncomingDTO incomingDTO = new IncomingDTO();
        incomingDTO.setIncoming(incoming);
        return incomingDTO;
    }
}
