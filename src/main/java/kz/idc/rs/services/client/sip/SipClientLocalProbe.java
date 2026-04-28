package kz.idc.rs.services.client.sip;

import kz.idc.dto.StatusDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

final class SipClientLocalProbe {

    private static final Duration CMD_TIMEOUT = Duration.ofSeconds(2);
    private static final String[] CANDIDATE_SERVICES = {
            "rescue-sip-client.service",
            "rescue-sip-client",
            "sowa_sip.service"
    };

    StatusDTO status() {
        for (String service : CANDIDATE_SERVICES) {
            try {
                if (isActive(service)) {
                    return StatusDTO.create(true);
                }
            } catch (Exception ignored) {
            }
        }
        return StatusDTO.create(false);
    }

    private boolean isActive(String service) throws Exception {
        Process p = new ProcessBuilder("systemctl", "is-active", service)
                .redirectErrorStream(true)
                .start();
        if (!p.waitFor(CMD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("Timeout checking " + service);
        }
        String line;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        return p.exitValue() == 0 && line != null && "active".equalsIgnoreCase(line.trim());
    }
}
