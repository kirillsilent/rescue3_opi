package kz.idc.rs.services.client.wifi;

import kz.idc.dto.wifi.AdvancedPointDTO;
import kz.idc.dto.wifi.AdvancedPointsDTO;
import kz.idc.dto.wifi.ConnectToPointDTO;
import kz.idc.dto.wifi.PointDTO;
import kz.idc.dto.wifi.PointStatusDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class WifiToolsLocalProbe {

    private static final Duration CMD_TIMEOUT = Duration.ofSeconds(10);

    PointDTO getPoint(String iface) throws Exception {
        if (!isWifiInterface(iface)) {
            return emptyPoint();
        }
        String ssid = null;
        try {
            List<String> out = run("iwgetid", iface, "--raw");
            if (!out.isEmpty()) {
                ssid = out.get(0).trim();
            }
        } catch (Exception ignored) {
        }
        if (ssid == null || ssid.isBlank()) {
            ssid = activeSsidFromNmcli(iface);
        }
        PointDTO dto = emptyPoint();
        if (ssid != null && !ssid.isBlank()) {
            dto.setSsid(ssid);
        }
        return dto;
    }

    AdvancedPointsDTO getPoints(String iface) throws Exception {
        if (!isWifiInterface(iface)) {
            return emptyPoints();
        }
        List<String> lines;
        try {
            lines = run("nmcli", "-t", "--escape", "yes", "-f", "SSID,SIGNAL,SECURITY", "dev", "wifi", "list", "ifname", iface, "--rescan", "yes");
        } catch (Exception e) {
            return emptyPoints();
        }
        Map<String, AdvancedPointDTO> bySsid = new LinkedHashMap<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            List<String> cols = splitEscapedColon(line, 3);
            if (cols.size() < 3) continue;
            String ssid = unescapeNmcli(cols.get(0)).trim();
            if (ssid.isBlank()) continue;
            int signal = parseInt(cols.get(1), 0);
            String security = unescapeNmcli(cols.get(2)).trim();
            boolean encrypted = !security.isBlank() && !"--".equals(security);

            AdvancedPointDTO current = bySsid.get(ssid);
            if (current == null || signal > current.getSignal()) {
                AdvancedPointDTO p = new AdvancedPointDTO();
                p.setSsid(ssid);
                p.setSignal(signal);
                p.setEncrypted(encrypted);
                bySsid.put(ssid, p);
            }
        }
        AdvancedPointsDTO dto = emptyPoints();
        dto.setPoints(new ArrayList<>(bySsid.values()));
        return dto;
    }

    PointStatusDTO getConnection(String iface, String point) throws Exception {
        if (!isWifiInterface(iface)) {
            return disconnected();
        }
        PointDTO current = getPoint(iface);
        boolean connected = current.getSsid() != null && current.getSsid().equals(point) && isWifiLinked(iface);
        PointStatusDTO dto = disconnected();
        dto.setConnected(connected);
        return dto;
    }

    PointStatusDTO connect(String iface, ConnectToPointDTO connectToPointDTO) throws Exception {
        if (!isWifiInterface(iface)) {
            return disconnected();
        }
        String ssid = connectToPointDTO.getSsid();
        String psk = connectToPointDTO.getPsk();
        if (ssid == null || ssid.isBlank()) {
            throw new IllegalArgumentException("ssid is required");
        }
        List<String> cmd = new ArrayList<>(List.of("nmcli", "device", "wifi", "connect", ssid, "ifname", iface));
        if (psk != null && !psk.isBlank()) {
            cmd.add("password");
            cmd.add(psk);
        }
        runWrite(cmd.toArray(String[]::new));
        return getConnection(iface, ssid);
    }

    String findFirstWifiInterface() {
        try (var stream = Files.list(Path.of("/sys/class/net"))) {
            return stream.map(p -> p.getFileName().toString())
                    .filter(this::isWifiInterface)
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isWifiLinked(String iface) {
        try {
            List<String> out = run("iw", iface, "link");
            return out.stream().noneMatch(s -> s != null && s.trim().equalsIgnoreCase("Not connected."));
        } catch (Exception e) {
            return false;
        }
    }

    private String activeSsidFromNmcli(String iface) {
        try {
            List<String> lines = run("nmcli", "-t", "--escape", "yes", "-f", "ACTIVE,SSID", "dev", "wifi", "list", "ifname", iface);
            for (String line : lines) {
                List<String> cols = splitEscapedColon(line, 2);
                if (cols.size() == 2 && "yes".equalsIgnoreCase(cols.get(0))) {
                    return unescapeNmcli(cols.get(1));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isWifiInterface(String iface) {
        return iface != null && !iface.isBlank() && iface.startsWith("w");
    }

    private PointDTO emptyPoint() {
        return new PointDTO();
    }

    private AdvancedPointsDTO emptyPoints() {
        AdvancedPointsDTO dto = new AdvancedPointsDTO();
        dto.setPoints(new ArrayList<>());
        return dto;
    }

    private PointStatusDTO disconnected() {
        PointStatusDTO dto = new PointStatusDTO();
        dto.setConnected(false);
        return dto;
    }

    private List<String> run(String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        if (!p.waitFor(CMD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("Timeout running " + cmd[0]);
        }
        List<String> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) out.add(line);
        }
        if (p.exitValue() != 0) {
            throw new IllegalStateException(cmd[0] + " exited with " + p.exitValue());
        }
        return out;
    }

    private List<String> runWrite(String... cmd) throws Exception {
        try {
            return run(cmd);
        } catch (Exception first) {
            String[] sudoCmd = new String[cmd.length + 2];
            sudoCmd[0] = "sudo";
            sudoCmd[1] = "-n";
            System.arraycopy(cmd, 0, sudoCmd, 2, cmd.length);
            return run(sudoCmd);
        }
    }

    private List<String> splitEscapedColon(String line, int maxParts) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                current.append(c);
                continue;
            }
            if (c == ':' && parts.size() < maxParts - 1) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    private String unescapeNmcli(String s) {
        return s.replace("\\:", ":").replace("\\\\", "\\");
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
