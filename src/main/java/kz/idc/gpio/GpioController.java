package kz.idc.gpio;

import io.micronaut.context.annotation.Requires;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.plugin.linuxfs.provider.gpio.digital.LinuxFsDigitalInputProvider;
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform;

import org.yaml.snakeyaml.Yaml;

@Singleton
@Requires(property = "gpio.enabled", value = "true")
public class GpioController {

    private static final int BUTTON_BCM_PIN = 4;
    // For Pi4J LinuxFS provider on this kernel, BCM4 is exposed as global GPIO 516 (gpiochip base 512 + 4).
    private static final int BUTTON_PIN = 516;
    private static final long BUTTON_DEBOUNCE_MS = 300L;
    private static final long SYSFS_POLL_INTERVAL_MS = 50L;
    private static final String MEDIAMTX_FILE = "/etc/mediamtx.yml";
    private static final long RINGBACK_TIMEOUT_SEC = 12L;
    private static final String RINGBACK_FILENAME = "ringback.wav";

    private Context pi4j;
    private final HttpClient http = HttpClient.newBuilder().build();
    private static final String SIP_CALL_URL = "http://127.0.0.1:8080/sip_client/call";
    private volatile boolean pollingActive;
    private Thread pollingThread;

    @PostConstruct
    public void init() {
        try {
            pi4j = Pi4J.newContextBuilder()
                    .add(new RaspberryPiPlatform())
                    .add(LinuxFsDigitalInputProvider.newInstance())
                    .build();

            var config = DigitalInput.newConfigBuilder(pi4j)
                    .id("button")
                    .address(BUTTON_PIN)
                    .pull(PullResistance.PULL_UP)
                    .debounce(BUTTON_DEBOUNCE_MS)
                    .build();

            DigitalInput input = pi4j.create(config);

            input.addListener(evt -> {
                DigitalState state = evt.state();
                if (state.equals(DigitalState.LOW)) {
                    System.out.println("GpioController: button pressed");
                    handlePress();
                }
            });

            System.out.println("GpioController: initialized, watching GPIO BCM" + BUTTON_BCM_PIN + " (linuxfs pin " + BUTTON_PIN + ")");
        } catch (Throwable t) {
            System.err.println("GpioController init failed: " + t.getMessage());
            startSysfsPollingFallback();
        }
    }

    private void handlePress() {
        try {
            List<String> cams = readCameras();
            String school = readSchool(cams);
            playRingbackIfExists();
            get(SIP_CALL_URL);
            if (cams.isEmpty()) {
                System.out.println("GpioController: no cameras found, SIP call sent only");
                return;
            }
            String camParam = String.join(",", cams);
            String encodedCam = URLEncoder.encode(camParam, "UTF-8");
            String encodedSchool = URLEncoder.encode(school == null ? "" : school, "UTF-8");
            post("http://127.0.0.1:5050/trigger?cam=" + encodedCam + "&school=" + encodedSchool);
            System.out.println("GpioController: SIP call + trigger posted for cams=" + camParam + " school=" + school);
        } catch (Exception e) {
            System.err.println("GpioController handlePress error: " + e.getMessage());
        }
    }

    private void playRingbackIfExists() {
        Path ringback = resolveRingbackPath();
        if (ringback == null) {
            System.out.println("GpioController: " + RINGBACK_FILENAME + " not found, skip pre-call ringback");
            return;
        }
        try {
            Process p = new ProcessBuilder("aplay", "-q", ringback.toString()).start();
            boolean finished = p.waitFor(RINGBACK_TIMEOUT_SEC, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                System.err.println("GpioController: ringback timeout, continue call");
            } else if (p.exitValue() != 0) {
                System.err.println("GpioController: ringback player exited with code " + p.exitValue());
            }
        } catch (Exception e) {
            System.err.println("GpioController: ringback playback error: " + e.getMessage());
        }
    }

    private Path resolveRingbackPath() {
        List<Path> candidates = List.of(
                Paths.get(System.getProperty("user.home", "."), "rescue", "audio", RINGBACK_FILENAME),
                Paths.get("/home/pi/rescue/audio", RINGBACK_FILENAME),
                Paths.get("/home/pi/rescue3.0/audio", RINGBACK_FILENAME),
                Paths.get(System.getProperty("user.dir", "."), "audio", RINGBACK_FILENAME)
        );
        for (Path p : candidates) {
            if (Files.exists(p) && Files.isRegularFile(p)) {
                return p;
            }
        }
        return null;
    }

    private void startSysfsPollingFallback() {
        Path valuePath = Paths.get("/sys/class/gpio/gpio" + BUTTON_PIN + "/value");
        if (!Files.exists(valuePath)) {
            System.err.println("GpioController fallback disabled: " + valuePath + " not found");
            return;
        }
        if (pollingThread != null && pollingThread.isAlive()) {
            return;
        }

        pollingActive = true;
        pollingThread = new Thread(() -> {
            String last = readSysfsValue(valuePath);
            long lastPressAt = 0L;
            System.out.println("GpioController: fallback polling enabled for GPIO BCM" + BUTTON_BCM_PIN + " via " + valuePath);
            while (pollingActive) {
                try {
                    String current = readSysfsValue(valuePath);
                    if (current != null) {
                        boolean fallingEdge = "1".equals(last) && "0".equals(current);
                        long now = System.currentTimeMillis();
                        if (fallingEdge && (now - lastPressAt) >= BUTTON_DEBOUNCE_MS) {
                            System.out.println("GpioController: button pressed (sysfs fallback)");
                            lastPressAt = now;
                            handlePress();
                        }
                        last = current;
                    }
                    Thread.sleep(SYSFS_POLL_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    System.err.println("GpioController fallback polling error: " + e.getMessage());
                }
            }
        }, "gpio-button-poll");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private String readSysfsValue(Path valuePath) {
        try {
            return Files.readString(valuePath).trim();
        } catch (Exception e) {
            System.err.println("GpioController fallback read error: " + e.getMessage());
            return null;
        }
    }

    private void post(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            http.sendAsync(req, BodyHandlers.discarding())
                    .thenAccept(resp -> {
                        if (resp.statusCode() >= 400) {
                            System.err.println("GpioController POST " + url + " -> HTTP " + resp.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                System.err.println("GpioController POST failed (" + url + "): " + ex.getMessage());
                return null;
            });
        } catch (Exception e) {
            System.err.println("GpioController POST exception: " + e.getMessage());
        }
    }

    private void get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            http.sendAsync(req, BodyHandlers.discarding())
                    .thenAccept(resp -> {
                        if (resp.statusCode() >= 400) {
                            System.err.println("GpioController GET " + url + " -> HTTP " + resp.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("GpioController GET failed (" + url + "): " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("GpioController GET exception: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readCameras() {
        try {
            Path p = Paths.get(MEDIAMTX_FILE);
            if (!Files.exists(p)) return Collections.emptyList();
            try (InputStream in = Files.newInputStream(p)) {
                Yaml yaml = new Yaml();
                Map<String, Object> map = yaml.load(in);
                if (map == null) return Collections.emptyList();
                Object pathsObj = map.get("paths");
                if (!(pathsObj instanceof Map)) return Collections.emptyList();
                Map<String, Object> paths = (Map<String, Object>) pathsObj;
                return new ArrayList<>(paths.keySet());
            }
        } catch (Exception e) {
            System.err.println("readCameras error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private String readSchool(List<String> cams) {
        try {
            Path p = Paths.get(resolveMetaConfigPath());
            if (!Files.exists(p)) return "Неизвестное учреждение";
            try (InputStream in = Files.newInputStream(p)) {
                Yaml yaml = new Yaml();
                Map<String, Object> meta = yaml.load(in);
                if (meta == null) return "Неизвестное учреждение";
                Object pathsObj = meta.get("paths");
                if (!(pathsObj instanceof Map)) return "Неизвестное учреждение";
                Map<String, Object> paths = (Map<String, Object>) pathsObj;
                for (String cam : cams) {
                    Object camCfgObj = paths.get(cam);
                    if (camCfgObj instanceof Map) {
                        Map<String, Object> camCfg = (Map<String, Object>) camCfgObj;
                        Object school = camCfg.get("school");
                        if (school != null) return school.toString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("readSchool error: " + e.getMessage());
        }
        return "Неизвестное учреждение";
    }

    private String resolveMetaConfigPath() {
        String envPath = System.getenv("CAMPORTAL_META");
        if (envPath != null && !envPath.isBlank()) {
            return envPath;
        }
        return System.getProperty("camportal.meta", "/home/orangepi/rescue/camportal/cameras_meta.yml");
    }

    @PreDestroy
    public void shutdown() {
        try {
            pollingActive = false;
            if (pollingThread != null) {
                pollingThread.interrupt();
            }
            if (pi4j != null) pi4j.shutdown();
        } catch (Exception e) {
            System.err.println("GpioController shutdown error: " + e.getMessage());
        }
    }
}
