package kz.idc.dto.settings;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class EventServerDTO {
    private String url;
    private String deviceSn;

    public static EventServerDTO create(String url, String deviceSn) {
        EventServerDTO dto = new EventServerDTO();
        dto.setUrl(url);
        dto.setDeviceSn(deviceSn);
        return dto;
    }
}

