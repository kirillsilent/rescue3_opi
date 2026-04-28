package kz.idc.utils.settings;

import kz.idc.dto.settings.SettingsDTO;

public class SettingsImpl implements Settings{

    @Override
    public SettingsDTO initDefault() {
        SettingsDTO settingsDTO = new SettingsDTO();
        settingsDTO.setVpnNetworkEnabled(DefaultSettings.vpn());
        settingsDTO.setCentralServer(DefaultSettings.webServer());
        settingsDTO.setSip(DefaultSettings.sip());
        settingsDTO.setIo(DefaultSettings.ioDevices());
        settingsDTO.setEventServerUrl("");
        settingsDTO.setDeviceSn("");
        return settingsDTO;
    }
}
