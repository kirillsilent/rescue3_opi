package kz.idc.dto.marquee;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Setter
@Getter
public class MarqueeDTO {
    private String marquee;

    public static MarqueeDTO create(String s){
        MarqueeDTO marqueeDTO = new MarqueeDTO();
        marqueeDTO.setMarquee(s);
        return marqueeDTO;
    }
}
