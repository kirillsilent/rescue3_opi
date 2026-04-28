package kz.idc.utils.downloader;

import io.reactivex.Observable;
import kz.idc.dto.audio.AudioDTO;
import kz.idc.dto.plan.PlanDTO;
import kz.idc.dto.settings.SettingsDTO;
import kz.idc.ws.WebSocket;

import java.util.List;

public interface Download {
    Observable<Object> downloadAudios(String uuid, String path, List<AudioDTO> audios, SettingsDTO settingsDTO, WebSocket webSocket);
    Observable<Object> downloadPlan(String uuid, String path, PlanDTO planDTO, SettingsDTO settingsDTO, WebSocket webSocket);
}
