package kz.idc.utils.storage;


import io.reactivex.Single;
import kz.idc.dto.IncidentDTO;
import kz.idc.dto.IsSettingsSetDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.network.VPNEnabledDTO;
import kz.idc.dto.settings.SettingsDTO;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipDTO;
import kz.idc.dto.sip.SipIgnoreAccDTO;
import kz.idc.dto.sip.acc.AccWithRegServerDTO;
import kz.idc.dto.sip.acc.AccountDTO;

public interface Storage {
    void createStorage();

    void delete();

    Single<IncidentDTO> getIncident();

    Single<IncidentDTO> setIncident(IncidentDTO incident);  // Добавлен метод setIncident

    Single<SettingsDTO> getSettings();

    Single<SettingsDTO> setSettings(SettingsDTO settings);

    Single<IODeviceDTO> setIO(IODeviceDTO io);

    Single<IODeviceDTO> getIO(String type);

    Single<RescueDTO> setRescueId(long rescueId);

    Single<RescueDTO> getRescueId();

    Single<IsSettingsSetDTO> isSettingsSet(SettingsDTO settingsDTO);

    boolean isSettingsSetBool();

    Single<AccountDTO> setSipAccount(AccountDTO sipAccount);

    Single<AccountDTO> getSipAccount();

    Single<String> getServerAddress();

    Single<ServerAddressDTO> setCentralServer(ServerAddressDTO centralServer);

    Single<String> setEventServerUrl(String url);

    Single<String> getEventServerUrl();

    Single<SipIgnoreAccDTO> setSipConfig(SipIgnoreAccDTO sipConfig);

    Single<SipDTO> getSip();

    Single<AccWithRegServerDTO> getSipAccountWithRegServer();

    Single<AccWithRegServerDTO> setSipAccountWithRegServer(AccWithRegServerDTO sipAccWithRegServerDTO);

    Single<VPNEnabledDTO> setEnabledVPN(VPNEnabledDTO vpnEnabledDTO);

    Single<VPNEnabledDTO> getEnabledVPN();
}
