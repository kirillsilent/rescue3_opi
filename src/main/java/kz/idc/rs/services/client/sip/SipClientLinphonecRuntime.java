package kz.idc.rs.services.client.sip;

import kz.idc.dto.StatusDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IOType;
import kz.idc.dto.sip.SipDTO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

final class SipClientLinphonecRuntime {

    interface Listener {
        void onRegistrationChanged(boolean ok);
        void onIncomingCall();
        void onCallEnded();
        void onCallConnected();
        void onError(String message);
    }

    private static final Logger log = LoggerFactory.getLogger(SipClientLinphonecRuntime.class);
    private static final Duration CMD_TIMEOUT = Duration.ofSeconds(4);
    private static final long POLL_MS = 2000L;
    private static final long IO_RETRY_WINDOW_MS = 45_000L;
    private static final long IO_RETRY_INTERVAL_MS = 3_000L;

    private final Storage storage = $Storage.mk();
    private final ReentrantLock cmdLock = new ReentrantLock();
    private final Listener listener;

    private volatile boolean quit;
    private volatile Thread loopThread;
    private volatile Boolean lastRegOk;
    private volatile String lastHook;
    private volatile boolean incomingSeen;
    private volatile boolean emergency;
    private volatile long retryIoUntilMs;
    private volatile long nextIoRetryAtMs;

    private volatile String username = "";
    private volatile String password = "";
    private volatile String hostname = "localhost";
    private volatile int remotePort = 5060;
    private volatile String operator = "";

    SipClientLinphonecRuntime(Listener listener) {
        this.listener = listener;
        reloadSipFromStorage();
    }

    synchronized void ensureStarted() {
        if (loopThread != null && loopThread.isAlive()) {
            return;
        }
        quit = false;
        loopThread = new Thread(this::runLoop, "sip-linphonec-runtime");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    synchronized void restart() {
        shutdown();
        ensureStarted();
    }

    synchronized void shutdown() {
        quit = true;
        linphonecsh(List.of("exit"));
        Thread t = loopThread;
        if (t != null && t.isAlive()) {
            try {
                t.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        loopThread = null;
    }

    StatusDTO status() {
        Boolean reg = lastRegOk;
        if (reg != null) {
            return StatusDTO.create(reg);
        }
        return StatusDTO.create(false);
    }

    void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    void startEmergency() {
        setEmergency(true);
        hangup();
    }

    void stopEmergency() {
        setEmergency(false);
    }

    void call() {
        ensureStarted();
        reloadSipFromStorage();
        ensureDaemon();
        applyIoFromStorage();
        linphonecsh(List.of("generic", "video on"));
        linphonecsh(List.of("generic", "selfview off"));
        hideVideoWindows(); // fallback if backend still creates a window
        String uri = "sip:" + safe(operator, username) + "@" + hostname + ":" + remotePort;
        CommandResult res = linphonecsh(List.of("dial", uri));
        hideVideoWindowsBurst();
        if (res.code != 0) {
            listener.onError("linphonecsh dial failed");
            throw new IllegalStateException("dial failed: " + res.output);
        }
    }

    void hangup() {
        ensureStarted();
        CommandResult res = linphonecsh(List.of("generic", "terminate"));
        if (res.code != 0) {
            log.warn("linphonecsh terminate rc={} out={}", res.code, res.output);
        }
    }

    void updateSipAccount() {
        reloadSipFromStorage();
        ensureStarted();
        ensureDaemon();
        doRegister();
    }

    void updateSipConfig() {
        reloadSipFromStorage();
        ensureStarted();
        ensureDaemon();
        doRegister();
    }

    void updateIO() {
        ensureStarted();
        ensureDaemon();
        scheduleIoRetryWindow();
        applyIoFromStorage();
    }

    private void runLoop() {
        try {
            ensureDaemon();
            scheduleIoRetryWindow();
            applyIoFromStorage();
        } catch (Exception e) {
            log.warn("Initial SIP runtime setup failed: {}", e.getMessage());
        }

        while (!quit) {
            try {
                retryIoIfNeeded();
                pollStatusOnce();
            } catch (Exception e) {
                log.warn("SIP poll failed: {}", e.getMessage());
            }
            try {
                Thread.sleep(POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void reloadSipFromStorage() {
        try {
            SipDTO sip = storage.getSip().blockingGet();
            if (sip != null) {
                if (sip.getAccount() != null) {
                    username = nvl(sip.getAccount().getAccount());
                    password = nvl(sip.getAccount().getPassword());
                }
                hostname = normalizeHost(nvl(sip.getHostname()));
                remotePort = sip.getPort() > 0 ? sip.getPort() : 5060;
                operator = nvl(sip.getOperator());
            }
        } catch (Exception e) {
            log.warn("Failed to load SIP config from storage: {}", e.getMessage());
        }
    }

    private void applyIoFromStorage() {
        String audioIn = ioByType(IOType.AUDIO_INPUT.DEVICE);
        String audioOut = ioByType(IOType.AUDIO_OUTPUT.DEVICE);
        String camera = ioByType(IOType.CAMERA.DEVICE);

        SoundcardApplyResult soundcardResult = applySoundcards(audioIn, audioOut);
        applyCamera(camera);
        if (!soundcardResult.fullyApplied) {
            scheduleIoRetryWindow();
            log.info("Audio devices are not ready yet, will retry binding. inputFound={}, outputFound={}",
                    soundcardResult.inputFound, soundcardResult.outputFound);
        } else {
            clearIoRetryWindow();
        }
    }

    private String ioByType(String type) {
        try {
            IODeviceDTO io = storage.getIO(type).blockingGet();
            return io == null ? "" : nvl(io.getDevice());
        } catch (Exception e) {
            return "";
        }
    }

    private SoundcardApplyResult applySoundcards(String audioIn, String audioOut) {
        CommandResult res = linphonecsh(List.of("generic", "soundcard list"));
        if (res.code != 0 && isPipeError(res.output)) {
            sleep(700);
            try {
                ensureDaemon();
            } catch (Exception ignored) {
            }
            res = linphonecsh(List.of("generic", "soundcard list"));
        }
        if (res.code != 0) {
            log.warn("soundcard list failed: {}", res.output);
            return SoundcardApplyResult.notApplied();
        }
        List<IndexedName> soundcards = parseIndexedList(res.output);
        Integer inIdx = findIndex(soundcards, audioIn);
        Integer outIdx = findIndex(soundcards, audioOut);
        if (inIdx != null) {
            linphonecsh(List.of("soundcard", "capture", String.valueOf(inIdx)));
        } else if (audioIn != null && !audioIn.isBlank()) {
            log.info("Linphone input soundcard '{}' is not available yet", audioIn);
        }
        if (outIdx != null) {
            linphonecsh(List.of("soundcard", "playback", String.valueOf(outIdx)));
            linphonecsh(List.of("soundcard", "ring", String.valueOf(outIdx)));
        } else if (audioOut != null && !audioOut.isBlank()) {
            log.info("Linphone output soundcard '{}' is not available yet", audioOut);
        }
        return SoundcardApplyResult.of(audioIn, audioOut, inIdx != null, outIdx != null);
    }

    private void retryIoIfNeeded() {
        long now = System.currentTimeMillis();
        if (retryIoUntilMs <= now || nextIoRetryAtMs > now) {
            return;
        }
        nextIoRetryAtMs = now + IO_RETRY_INTERVAL_MS;
        try {
            applyIoFromStorage();
        } catch (Exception e) {
            log.warn("Deferred audio rebind failed: {}", e.getMessage());
        }
    }

    private void scheduleIoRetryWindow() {
        long now = System.currentTimeMillis();
        retryIoUntilMs = now + IO_RETRY_WINDOW_MS;
        nextIoRetryAtMs = now;
    }

    private void clearIoRetryWindow() {
        retryIoUntilMs = 0L;
        nextIoRetryAtMs = 0L;
    }

    private void applyCamera(String camera) {
        if (camera == null || camera.isBlank()) {
            return;
        }
        CommandResult res = linphonecsh(List.of("generic", "webcam list"));
        if (res.code != 0 && isPipeError(res.output)) {
            sleep(700);
            try {
                ensureDaemon();
            } catch (Exception ignored) {
            }
            res = linphonecsh(List.of("generic", "webcam list"));
        }
        if (res.code != 0) {
            log.warn("webcam list failed: {}", res.output);
            return;
        }
        List<IndexedName> webcams = parseIndexedList(res.output);
        Integer idx = findIndex(webcams, camera);
        if (idx != null) {
            linphonecsh(List.of("generic", "webcam use " + idx));
        } else {
            log.warn("Camera {} not found in linphone webcam list", camera);
        }
    }

    private Integer findIndex(List<IndexedName> list, String needle) {
        if (needle == null || needle.isBlank()) {
            return null;
        }
        String n = needle.toLowerCase(Locale.ROOT);
        for (IndexedName item : list) {
            if (item.name.toLowerCase(Locale.ROOT).equals(n) || item.name.toLowerCase(Locale.ROOT).contains(n)) {
                return item.index;
            }
        }
        return null;
    }

    private List<IndexedName> parseIndexedList(String text) {
        List<IndexedName> out = new ArrayList<>();
        for (String ln : text.split("\\R")) {
            int i = ln.indexOf(':');
            if (i <= 0) {
                continue;
            }
            String left = ln.substring(0, i).trim();
            if (!left.chars().allMatch(Character::isDigit)) {
                continue;
            }
            try {
                out.add(new IndexedName(Integer.parseInt(left), ln.substring(i + 1).trim()));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void pollStatusOnce() {
        hideVideoWindows();
        CommandResult reg = linphonecsh(List.of("status", "register"));
        if (reg.code != 0 && isPipeError(reg.output)) {
            ensureDaemon();
            return;
        }
        Boolean regOk = parseRegisterStatus(reg.output);
        if (regOk != null && !Objects.equals(regOk, lastRegOk)) {
            lastRegOk = regOk;
            listener.onRegistrationChanged(regOk);
        }

        CommandResult hook = linphonecsh(List.of("status", "hook"));
        if (hook.code != 0 && isPipeError(hook.output)) {
            ensureDaemon();
            return;
        }
        String hout = hook.output.toLowerCase(Locale.ROOT);
        String hookState = null;
        if (hout.contains("off-hook")) {
            hookState = "connected";
        } else if (hout.contains("on-hook")) {
            hookState = "idle";
        }
        if (hookState != null && !hookState.equals(lastHook)) {
            String prev = lastHook;
            lastHook = hookState;
            if ("connected".equals(hookState)) {
                hideVideoWindowsBurst();
                listener.onCallConnected();
            } else if ("idle".equals(hookState) && "connected".equals(prev) && !emergency) {
                hideVideoWindows();
                listener.onCallEnded();
            }
        }

        pollCallsOnce();
    }

    private void pollCallsOnce() {
        CommandResult calls = linphonecsh(List.of("generic", "calls"));
        if (calls.code != 0 && isPipeError(calls.output)) {
            ensureDaemon();
            return;
        }
        String out = nvl(calls.output).toLowerCase(Locale.ROOT);
        boolean incomingRinging = out.contains("incomingreceived") || out.contains("incomingearlymedia");
        if (incomingRinging && !incomingSeen) {
            incomingSeen = true;
            try {
                listener.onIncomingCall();
            } catch (Exception ignored) {
            }
            CommandResult ans = linphonecsh(List.of("generic", "answer"));
            hideVideoWindowsBurst();
            if (ans.code != 0) {
                log.warn("linphone answer rc={} out={}", ans.code, ans.output);
            }
        }

        boolean anyActiveCall = out.contains("outgoing") || out.contains("incoming") || out.contains("streamsrunning")
                || out.contains("paused") || out.contains("connected");
        if (anyActiveCall) {
            // Linphone may re-show preview/remote windows after media starts; keep forcing headless mode.
            hideVideoWindows();
        }
        if (!anyActiveCall) {
            incomingSeen = false;
        }
    }

    private void ensureDaemon() {
        reloadSipFromStorage();
        CommandResult probe = linphonecsh(List.of("status", "register"));
        if (!isPipeError(probe.output) && looksLikeRegisterStatus(probe.output)) {
            doRegister();
            return;
        }

        // Stop any existing daemon so the new one starts with our env and config (MSNullDisplay, no DISPLAY)
        linphonecsh(List.of("exit"));
        sleep(1200);

        ensureLinphoneDataDirs();
        ensureHeadlessVideoLinphonerc();
        CommandResult init = linphonecsh(List.of(
                "init", "-c", linphonercPath(), "-d", "2", "-l", "/tmp/linphonec.log", "-a", "-V"
        ));
        String initOut = init.output.toLowerCase(Locale.ROOT);
        if (init.code != 0 && !initOut.contains("running linphonec has been found")) {
            listener.onError("linphonec init failed");
            throw new IllegalStateException("linphone init failed: " + init.output);
        }
        sleep(1500);
        hideVideoWindows();
        doRegister();
    }

    private void ensureHeadlessVideoLinphonerc() {
        Path rcPath = Path.of(linphonercPath());
        try {
            List<String> lines = Files.exists(rcPath)
                    ? new ArrayList<>(Files.readAllLines(rcPath, StandardCharsets.UTF_8))
                    : new ArrayList<>();
            List<String> updated = upsertVideoSection(lines);
            updated = upsertMediastreamerSection(updated);
            updated = upsertMs2Section(updated);
            if (!lines.equals(updated)) {
                Files.write(rcPath, updated, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("Failed to update {} video section: {}", rcPath, e.getMessage());
        }
    }

    private void ensureLinphoneDataDirs() {
        try {
            Path home = Path.of(System.getProperty("user.home", "/home/pi"));
            Files.createDirectories(home.resolve(".local/share/linphone"));
            Files.createDirectories(home.resolve(".linphone-usr-crt"));
        } catch (Exception e) {
            log.warn("Failed to create linphone data dirs: {}", e.getMessage());
        }
    }

    private List<String> upsertVideoSection(List<String> src) {
        List<String> lines = src == null ? new ArrayList<>() : new ArrayList<>(src);
        Map<String, String> required = new LinkedHashMap<>();
        required.put("enabled", "1");
        required.put("capture", "1");
        required.put("display", "0");
        required.put("show_local", "0");
        required.put("self_view", "0");

        int sectionStart = -1;
        int sectionEnd = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                if ("[video]".equalsIgnoreCase(s)) {
                    sectionStart = i;
                    for (int j = i + 1; j < lines.size(); j++) {
                        String t = lines.get(j).trim();
                        if (t.startsWith("[") && t.endsWith("]")) {
                            sectionEnd = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (sectionStart < 0) {
            if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
                lines.add("");
            }
            lines.add("[video]");
            for (Map.Entry<String, String> e : required.entrySet()) {
                lines.add(e.getKey() + "=" + e.getValue());
            }
            return lines;
        }

        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = sectionStart + 1; i < sectionEnd; i++) {
            String raw = lines.get(i);
            String s = raw.trim();
            if (s.isEmpty() || s.startsWith("#") || s.startsWith(";")) {
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = s.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            if (required.containsKey(key) && !seen.containsKey(key)) {
                seen.put(key, i);
            }
        }

        for (Map.Entry<String, String> e : required.entrySet()) {
            Integer idx = seen.get(e.getKey());
            String line = e.getKey() + "=" + e.getValue();
            if (idx != null) {
                lines.set(idx, line);
            } else {
                lines.add(sectionEnd, line);
                sectionEnd++;
            }
        }
        return lines;
    }

    private List<String> upsertMediastreamerSection(List<String> src) {
        List<String> lines = src == null ? new ArrayList<>() : new ArrayList<>(src);
        Map<String, String> required = new LinkedHashMap<>();
        required.put("video_display_filter", "MSNullDisplay");

        int sectionStart = -1;
        int sectionEnd = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                if ("[mediastreamer]".equalsIgnoreCase(s)) {
                    sectionStart = i;
                    for (int j = i + 1; j < lines.size(); j++) {
                        String t = lines.get(j).trim();
                        if (t.startsWith("[") && t.endsWith("]")) {
                            sectionEnd = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (sectionStart < 0) {
            if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
                lines.add("");
            }
            lines.add("[mediastreamer]");
            for (Map.Entry<String, String> e : required.entrySet()) {
                lines.add(e.getKey() + "=" + e.getValue());
            }
            return lines;
        }

        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = sectionStart + 1; i < sectionEnd; i++) {
            String raw = lines.get(i);
            String s = raw.trim();
            if (s.isEmpty() || s.startsWith("#") || s.startsWith(";")) {
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = s.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            if (required.containsKey(key) && !seen.containsKey(key)) {
                seen.put(key, i);
            }
        }

        for (Map.Entry<String, String> e : required.entrySet()) {
            Integer idx = seen.get(e.getKey());
            String line = e.getKey() + "=" + e.getValue();
            if (idx != null) {
                lines.set(idx, line);
            } else {
                lines.add(sectionEnd, line);
                sectionEnd++;
            }
        }
        return lines;
    }

    private List<String> upsertMs2Section(List<String> src) {
        List<String> lines = src == null ? new ArrayList<>() : new ArrayList<>(src);
        Map<String, String> required = new LinkedHashMap<>();
        required.put("video_display_filter", "MSNullDisplay");

        int sectionStart = -1;
        int sectionEnd = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                if ("[ms2]".equalsIgnoreCase(s)) {
                    sectionStart = i;
                    for (int j = i + 1; j < lines.size(); j++) {
                        String t = lines.get(j).trim();
                        if (t.startsWith("[") && t.endsWith("]")) {
                            sectionEnd = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (sectionStart < 0) {
            if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
                lines.add("");
            }
            lines.add("[ms2]");
            for (Map.Entry<String, String> e : required.entrySet()) {
                lines.add(e.getKey() + "=" + e.getValue());
            }
            return lines;
        }

        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = sectionStart + 1; i < sectionEnd; i++) {
            String raw = lines.get(i);
            String s = raw.trim();
            if (s.isEmpty() || s.startsWith("#") || s.startsWith(";")) {
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = s.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            if (required.containsKey(key) && !seen.containsKey(key)) {
                seen.put(key, i);
            }
        }

        for (Map.Entry<String, String> e : required.entrySet()) {
            Integer idx = seen.get(e.getKey());
            String line = e.getKey() + "=" + e.getValue();
            if (idx != null) {
                lines.set(idx, line);
            } else {
                lines.add(sectionEnd, line);
                sectionEnd++;
            }
        }
        return lines;
    }

    private void doRegister() {
        String user = nvl(username);
        String pass = nvl(password);
        String host = nvl(hostname);
        int port = remotePort;
        if (user.isBlank() || host.isBlank() || port <= 0) {
            return;
        }
        String target = port == 5060 ? host : host + ":" + port;
        CommandResult reg = linphonecsh(List.of(
                "register",
                "--host", target,
                "--username", user,
                "--password", pass
        ));
        if (registerCommandSucceeded(reg)) {
            linphonecsh(List.of("generic", "autoanswer enable"));
            return;
        }
        if (isPipeError(reg.output)) {
            sleep(800);
            reg = linphonecsh(List.of(
                    "register", "--host", target, "--username", user, "--password", pass
            ));
        }
        if (reg.code != 0) {
            listener.onError("linphone register failed");
            log.warn("linphone register failed rc={} out={}", reg.code, reg.output);
        }
    }

    private void hideVideoWindows() {
        // Some linphone builds ignore one of these keys; send several compatible variants.
        linphonecsh(List.of("generic", "param video display 0"));
        linphonecsh(List.of("generic", "param video show_local 0"));
        linphonecsh(List.of("generic", "param video self_view 0"));
        linphonecsh(List.of("generic", "param video preview 0"));
        linphonecsh(List.of("generic", "pwindow integrated"));
        linphonecsh(List.of("generic", "pwindow size 1 1"));
        linphonecsh(List.of("generic", "vwindow size 1 1"));
        linphonecsh(List.of("generic", "pwindow pos 10000 10000"));
        linphonecsh(List.of("generic", "vwindow pos 10000 10000"));
        linphonecsh(List.of("generic", "pwindow hide"));
        linphonecsh(List.of("generic", "vwindow hide"));
    }

    /** Repeatedly hide video windows for ~1s to catch any delayed window creation (e.g. after media start). */
    private void hideVideoWindowsBurst() {
        for (int i = 0; i < 12; i++) {
            hideVideoWindows();
            sleep(80);
        }
    }

    private Boolean parseRegisterStatus(String outRaw) {
        String out = nvl(outRaw).toLowerCase(Locale.ROOT);
        if (out.strip().startsWith("registered") && !out.contains("registered=0") && !out.contains("registered=-1")) {
            return true;
        }
        if (out.contains("unregistered") || out.contains("registered=0") || out.contains("registered=-1")
                || out.contains("failed") || out.contains("not registered")) {
            return false;
        }
        return null;
    }

    private boolean looksLikeRegisterStatus(String outRaw) {
        String out = nvl(outRaw).toLowerCase(Locale.ROOT);
        return out.contains("registered") || out.contains("unregistered") || out.contains("not registered");
    }

    private boolean registerCommandSucceeded(CommandResult reg) {
        if (reg == null) {
            return false;
        }
        if (reg.code == 0) {
            return true;
        }
        String out = nvl(reg.output).toLowerCase(Locale.ROOT);
        return out.contains("registered") || out.contains("registering");
    }

    private String linphonercPath() {
        return System.getProperty("user.home", "/home/pi") + "/.linphonerc";
    }

    private boolean isPipeError(String out) {
        String s = nvl(out).toLowerCase(Locale.ROOT);
        return s.contains("failed to connect pipe") || s.contains("connection refused");
    }

    private CommandResult linphonecsh(List<String> args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("linphonecsh");
        cmd.addAll(args);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        // No real display: X11/Wayland cannot connect, so mediastreamer uses MSNullDisplay from config
        pb.environment().put("DISPLAY", ":999");
        pb.environment().remove("WAYLAND_DISPLAY");
        pb.environment().remove("XAUTHORITY");
        pb.environment().remove("DESKTOP_SESSION");
        // do not remove XDG_RUNTIME_DIR, DBUS_SESSION_BUS_ADDRESS — sound needs them

        cmdLock.lock();
        try {
            Process p = pb.start();
            if (!p.waitFor(CMD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                return new CommandResult(124, "timeout");
            }
            StringBuilder out = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    out.append(line).append('\n');
                }
            }
            return new CommandResult(p.exitValue(), out.toString());
        } catch (Exception e) {
            return new CommandResult(1, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        } finally {
            cmdLock.unlock();
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String safe(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? nvl(fallback) : preferred;
    }

    private String normalizeHost(String raw) {
        String s = nvl(raw).trim();
        if (s.isBlank()) {
            return "";
        }
        s = s.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
        int slash = s.indexOf('/');
        if (slash >= 0) {
            s = s.substring(0, slash);
        }
        return s;
    }

    private static final class IndexedName {
        final int index;
        final String name;

        IndexedName(int index, String name) {
            this.index = index;
            this.name = name == null ? "" : name;
        }
    }

    private static final class SoundcardApplyResult {
        final boolean inputFound;
        final boolean outputFound;
        final boolean fullyApplied;

        private SoundcardApplyResult(boolean inputFound, boolean outputFound, boolean fullyApplied) {
            this.inputFound = inputFound;
            this.outputFound = outputFound;
            this.fullyApplied = fullyApplied;
        }

        static SoundcardApplyResult of(String audioIn, String audioOut, boolean inputFound, boolean outputFound) {
            boolean inputRequired = audioIn != null && !audioIn.isBlank();
            boolean outputRequired = audioOut != null && !audioOut.isBlank();
            boolean fullyApplied = (!inputRequired || inputFound) && (!outputRequired || outputFound);
            return new SoundcardApplyResult(inputFound, outputFound, fullyApplied);
        }

        static SoundcardApplyResult notApplied() {
            return new SoundcardApplyResult(false, false, false);
        }
    }

    private static final class CommandResult {
        final int code;
        final String output;

        CommandResult(int code, String output) {
            this.code = code;
            this.output = output == null ? "" : output;
        }
    }
}
