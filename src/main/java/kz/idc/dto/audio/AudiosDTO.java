package kz.idc.dto.audio;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.PathDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Introspected
@Getter
@Setter
public class AudiosDTO extends PathDTO {
    private List<AudioDTO> audios;
}
