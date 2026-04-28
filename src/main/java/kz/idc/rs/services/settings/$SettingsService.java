package kz.idc.rs.services.settings;

public class $SettingsService {

    private static SettingsService settingsService;

    public static SettingsService mk(){
        if(settingsService == null){
            settingsService = new SettingsServiceImpl();
        }
        return settingsService;
    }
}
