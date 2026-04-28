package kz.idc.utils.settings;


public class $Settings {

    private static SettingsImpl mSettingsImpl;
    private $Settings(){}

    public static SettingsImpl mk(){
        if(mSettingsImpl == null){
            mSettingsImpl = new SettingsImpl();
        }
        return mSettingsImpl;
    }
}
