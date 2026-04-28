package kz.idc.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Map;

@Filter({
        "/settings",
        "/settings_api/**",
        "/incident_settings/**",
        "/hardware/**",
        "/wifi/**",
        "/network/**",
        "/sip_client/restart",
        "/sip_client/stop"
})
public class SettingsAuthFilter implements HttpServerFilter {
    private static final String AUTH_REQUIRED_HEADER = "X-Settings-Auth-Required";
    private static final Logger LOG = LoggerFactory.getLogger(SettingsAuthFilter.class);

    private final SettingsAuthService auth;

    public SettingsAuthFilter(SettingsAuthService auth) {
        this.auth = auth;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String path = request.getPath();
        String method = request.getMethod().name();

        // allow CORS preflight
        if (method.equalsIgnoreCase("OPTIONS")) {
            return Flowable.fromPublisher(chain.proceed(request));
        }

        // Allow public status endpoint on the main screen
        if ("/network/status".equals(path)) {
            return Flowable.fromPublisher(chain.proceed(request));
        }

        boolean authorized = auth.isAuthorized(request);
        LOG.info("SettingsAuthFilter: {} {} authorized={} cookie={}",
                method, path, authorized, summarizeCookieHeaders(request.getHeaders().getAll("Cookie")));

        if (authorized) {
            return Flowable.fromPublisher(chain.proceed(request));
        }

        // Allow login page (full-page form) without auth
        if ("/settings/login".equals(path)) {
            return Flowable.fromPublisher(chain.proceed(request));
        }

        // Unauthorized
        boolean wantsHtml = request.getHeaders().accept().stream()
                .anyMatch(mt -> mt.equals(MediaType.TEXT_HTML_TYPE));

        if (wantsHtml) {
            // Redirect to dedicated login page instead of showing settings (which would show modal).
            if ("/settings".equals(path)) {
                LOG.info("SettingsAuthFilter: redirecting to /settings/login for {} {}", method, path);
                return Flowable.just(HttpResponse.redirect(java.net.URI.create("/settings/login"))
                        .header(AUTH_REQUIRED_HEADER, "1"));
            }
            LOG.info("SettingsAuthFilter: returning 401 HTML for {} {}", method, path);
            return Flowable.just(HttpResponse.status(HttpStatus.UNAUTHORIZED)
                    .header(AUTH_REQUIRED_HEADER, "1")
                    .contentType(MediaType.TEXT_HTML_TYPE)
                    .body("<html><body style='font-family:Verdana,sans-serif'>401 Unauthorized</body></html>"));
        }

        LOG.info("SettingsAuthFilter: returning 401 JSON for {} {}", method, path);
        return Flowable.just(HttpResponse.status(HttpStatus.UNAUTHORIZED)
                .header(AUTH_REQUIRED_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .body(Map.of("ok", false, "message", "Требуется пароль для настроек")));
    }

    private static String summarizeCookieHeaders(List<String> headers) {
        if (headers == null || headers.isEmpty()) return "<none>";
        String joined = String.join(" | ", headers);
        if (joined.isBlank()) return "<empty>";
        return joined.length() > 300 ? joined.substring(0, 300) + "...(truncated)" : joined;
    }
}
