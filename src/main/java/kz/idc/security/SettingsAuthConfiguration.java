package kz.idc.security;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(SettingsAuthConfiguration.PREFIX)
public class SettingsAuthConfiguration {
    public static final String PREFIX = "settings-auth";

    /**
     * Plain password from config/env.
     * Keep it short and rotate if needed.
     */
    private String password;

    /**
     * Salt to make cookie token non-trivial.
     */
    private String salt;

    /**
     * Cookie name.
     */
    private String cookieName = "settings_auth";

    /**
     * Cookie lifetime.
     */
    private int maxAgeHours = 24;
}

