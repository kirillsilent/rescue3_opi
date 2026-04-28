package kz.idc.security;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller("/settings_auth")
public class SettingsAuthController {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsAuthController.class);

    private final SettingsAuthService auth;

    public SettingsAuthController(SettingsAuthService auth) {
        this.auth = auth;
    }

    @Get(uri = "/status", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> status(io.micronaut.http.HttpRequest<?> request) {
        boolean authorized = auth.isAuthorized(request);
        String cookieHeader = request.getHeaders().get("Cookie");
        LOG.info("SettingsAuthController.status: authorized={} cookieHeader={}",
                authorized,
                cookieHeader == null ? "<none>" : cookieHeader);
        return HttpResponse.ok(Map.of("authorized", authorized));
    }

    @Post(uri = "/login", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> login(@Body SettingsLoginDTO dto) {
        String pwd = dto == null ? null : dto.getPassword();
        int length = pwd == null ? 0 : pwd.length();
        LOG.info("SettingsAuthController.login: attempt passwordLength={}", length);
        if (!auth.verifyPassword(pwd == null ? "" : pwd)) {
            LOG.info("SettingsAuthController.login: rejected");
            return HttpResponse.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "Неверный пароль"));
        }

        String token = auth.tokenFor(pwd == null ? "" : pwd);
        Cookie cookie = Cookie.of("settings_auth", token)
                .path("/")
                .httpOnly(true)
                .maxAge(86400)
                .sameSite(SameSite.Lax);

        LOG.info("SettingsAuthController.login: success cookieName={} maxAge={}s",
                auth.cookieName(), auth.maxAgeSeconds());
        return HttpResponse.ok(Map.of("ok", true)).cookie(cookie);
    }

    @Post(uri = "/logout", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> logout() {
        LOG.info("SettingsAuthController.logout: clearing cookie {}", auth.cookieName());
        Cookie cookie = Cookie.of(auth.cookieName(), "")
                .path("/")
                .httpOnly(true)
                .maxAge(0);
        return HttpResponse.ok(Map.of("ok", true)).cookie(cookie);
    }
}
