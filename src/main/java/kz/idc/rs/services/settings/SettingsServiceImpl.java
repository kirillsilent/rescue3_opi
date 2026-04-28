package kz.idc.rs.services.settings;

import io.reactivex.Maybe;
import kz.idc.dto.IsSettingsSetDTO;
import kz.idc.dto.settings.ApiDTO;
import kz.idc.dto.settings.EventServerDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;

public class SettingsServiceImpl implements SettingsService{

    private final Storage storage = $Storage.mk();

    @Override
    public Maybe<ApiDTO> getAllApiSettings() {
        return storage.getSettings().toMaybe()
                .flatMap(settingsDTO -> create -> create.onSuccess(ApiDTO.create(
                        settingsDTO.getRescue(),
                        settingsDTO.getCentralServer(),
                        settingsDTO.getSip(),
                        settingsDTO.getEventServerUrl(),
                        settingsDTO.getDeviceSn()
                )));
    }

    @Override
    public Maybe<SipIgnoreAccDTO> setSipConfigWithoutAcc(SipIgnoreAccDTO sipIgnoreAccDTO) {
        return storage.setSipConfig(sipIgnoreAccDTO).toMaybe();
    }

    @Override
    public Maybe<EventServerDTO> setEventServerUrl(EventServerDTO eventServerDTO) {
        final String url = eventServerDTO == null ? null : eventServerDTO.getUrl();
        final String deviceSn = eventServerDTO == null ? null : eventServerDTO.getDeviceSn();

        return storage.getSettings().toMaybe()
                .flatMap(settings -> {
                    if (url != null) settings.setEventServerUrl(url);
                    if (deviceSn != null) settings.setDeviceSn(deviceSn);
                    return storage.setSettings(settings).toMaybe();
                })
                .map(saved -> EventServerDTO.create(
                        saved.getEventServerUrl(),
                        saved.getDeviceSn()
                ));
    }

    @Override
    public Maybe<IsSettingsSetDTO> isSettingsSet() {
        return storage.getSettings().toMaybe()
                .flatMap(settingsDTO -> storage.isSettingsSet(settingsDTO).toMaybe());
    }
}
