package kz.idc.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class DownloadDTO {

    private boolean isDownloaded;
    private String type;

    public static DownloadDTO create(boolean isDownloaded, String type){
        DownloadDTO downloadDTO = new DownloadDTO();
        downloadDTO.setDownloaded(isDownloaded);
        downloadDTO.setType(type);
        return downloadDTO;
    }
}
