package kz.idc.dto.audio;

import io.micronaut.core.annotation.Introspected;
import kz.idc.dto.io.IODeviceDTO;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class PlayDTO extends IODeviceDTO {
    private String track;

    public static PlayDTO create(IODeviceDTO ioDeviceDTO, String track){
        PlayDTO playDTO = new PlayDTO();
        playDTO.setTrack(track);
        playDTO.setDevice(ioDeviceDTO.getDevice());
        playDTO.setType(ioDeviceDTO.getType());
        return playDTO;
    }
}
