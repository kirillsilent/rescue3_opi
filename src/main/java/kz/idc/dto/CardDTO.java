package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class CardDTO {
    private String cardStatus;

    public static CardDTO create(String cardStatus){
        CardDTO cardDTO = new CardDTO();
        cardDTO.setCardStatus(cardStatus);
        return cardDTO;
    }
}
