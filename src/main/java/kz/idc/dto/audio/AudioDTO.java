package kz.idc.dto.audio;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
@EqualsAndHashCode(of = {"name", "checksum"})
public class AudioDTO {
    private String name;
    private String checksum;

    public static AudioDTO create(String name, String checksum) {
        AudioDTO audioDTO = new AudioDTO();
        audioDTO.setName(name);
        audioDTO.setChecksum(checksum);
        return audioDTO;
    }
}
