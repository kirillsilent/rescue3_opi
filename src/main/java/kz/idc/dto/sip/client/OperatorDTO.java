package kz.idc.dto.sip.client;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class OperatorDTO {

    private String operator;

    public static OperatorDTO create(String operator){
        OperatorDTO configDTO = new OperatorDTO();
        configDTO.setOperator(operator);
        return configDTO;
    }
}
