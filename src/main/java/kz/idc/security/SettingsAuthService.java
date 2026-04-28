package kz.idc.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Singleton
public class SettingsAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsAuthService.class);

    private final SettingsAuthConfiguration cfg;

    public SettingsAuthService(SettingsAuthConfiguration cfg) {
        this.cfg = cfg;
    }

    public boolean isEnabled() {
        return cfg.getPassword() != null && !cfg.getPassword().isBlank();
    }

    public boolean verifyPassword(String password) {
        if (!isEnabled()) return true;
        boolean ok = cfg.getPassword().equals(password);
        LOG.info("SettingsAuthService.verifyPassword: enabled=true ok={} inputLength={}",
                ok, password == null ? 0 : password.length());
        return ok;
    }

    public String cookieName() {
        return cfg.getCookieName();
    }

    public int maxAgeSeconds() {
        return Math.max(1, cfg.getMaxAgeHours()) * 3600;
    }

    public String expectedToken() {
        if (!isEnabled()) return "disabled";
        return tokenFor(cfg.getPassword());
    }

    public String tokenFor(String password) {
        // token = base64url(sha256(password + ":" + salt))
        String salt = cfg.getSalt() == null ? "" : cfg.getSalt();
        byte[] digest = sha256((password == null ? "" : password) + ":" + salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public boolean isAuthorized(HttpRequest<?> request) {
        if (!isEnabled()) return true;
        String expected = expectedToken();

        // Be tolerant to duplicate cookies with the same name but different Path/old values.
        // If at least one cookie value matches expected token, authorize request.
        List<String> rawCookieHeaders = request.getHeaders().getAll("Cookie");
        for (String header : rawCookieHeaders) {
            if (header == null || header.isBlank()) continue;
            String[] parts = header.split(";");
            for (String p : parts) {
                String part = p == null ? "" : p.trim();
                String prefix = cookieName() + "=";
                if (part.startsWith(prefix)) {
                    String value = part.substring(prefix.length());
                    if (expected.equals(value)) {
                        LOG.info("SettingsAuthService.isAuthorized: matched token from raw Cookie header");
                        return true;
                    }
                }
            }
        }

        Optional<Cookie> c = request.getCookies().findCookie(cookieName());
        boolean ok = c.map(cookie -> expected.equals(cookie.getValue())).orElse(false);
        LOG.info("SettingsAuthService.isAuthorized: fallback cookie object present={} ok={}",
                c.isPresent(), ok);
        return ok;
    }

    private static byte[] sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
