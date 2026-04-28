package kz.idc.rs.services.client.network;

import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.network.NetworkConfigDTO;
import kz.idc.dto.network.NetworkDevicesDTO;
import kz.idc.dto.network.NetworkIPMacDTO;
import kz.idc.dto.network.NetworkRouteDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class NetworkToolsLocalProbe {

    private static final Duration CMD_TIMEOUT = Duration.ofSeconds(2);

    NetworkDevicesDTO getNetworkInterfaces(String type) throws Exception {
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(Path.of("/sys/class/net"))) {
            stream.map(p -> p.getFileName().toString())
                    .filter(this::isUserFacingNetIf)
                    .filter(iface -> matchesType(type, iface))
                    .sorted(Comparator.naturalOrder())
                    .forEach(names::add);
        }
        return NetworkDevicesDTO.create(type, names);
    }

    NetworkConfigDTO getNetwork(IODeviceDTO io) throws Exception {
        if (io == null || io.getDevice() == null || io.getDevice().isBlank()) {
            throw new IllegalArgumentException("Network interface not selected");
        }
        String iface = io.getDevice();
        NetworkConfigDTO dto = new NetworkConfigDTO();
        dto.setIface(iface);

        IpAndMask ip = getIpv4AndNetmask(iface);
        dto.setIp(ip.ip);
        dto.setNetmask(ip.netmask);
        dto.setGateway(getDefaultGateway(iface));
        dto.setVpn(getIpv4Only("wg0"));
        dto.setStaticIp(isStaticIp(iface));

        List<String> dns = getDnsServers();
        dto.setDns1(dns.size() > 0 ? dns.get(0) : null);
        dto.setDns2(dns.size() > 1 ? dns.get(1) : null);
        return dto;
    }

    NetworkRouteDTO isDefaultRoute(IODeviceDTO io) throws Exception {
        if (io == null || io.getDevice() == null) {
            throw new IllegalArgumentException("Network interface not selected");
        }
        String currentDefault = getDefaultRouteInterface();
        NetworkRouteDTO dto = new NetworkRouteDTO();
        dto.setDefaultRoute(io.getDevice().equals(currentDefault));
        return dto;
    }

    Object setNetworkConfig(NetworkConfigDTO net) throws Exception {
        if (net == null || net.getIface() == null || net.getIface().isBlank()) {
            throw new IllegalArgumentException("iface is required");
        }
        String iface = net.getIface();
        String con = ensureConnectionName(iface);
        if (net.isStaticIp()) {
            String ip = net.getIp();
            String mask = net.getNetmask();
            if (ip == null || ip.isBlank() || mask == null || mask.isBlank()) {
                throw new IllegalArgumentException("ip and netmask are required for static config");
            }
            String address = ip + "/" + maskToPrefix(mask);
            List<String> cmd = new ArrayList<>(List.of(
                    "nmcli", "con", "modify", con,
                    "ipv4.method", "manual",
                    "ipv4.addresses", address
            ));
            if (net.getGateway() != null && !net.getGateway().isBlank()) {
                cmd.add("ipv4.gateway");
                cmd.add(net.getGateway());
            } else {
                cmd.add("ipv4.gateway");
                cmd.add("");
            }
            String dns = joinDns(net.getDns1(), net.getDns2());
            cmd.add("ipv4.dns");
            cmd.add(dns);
            cmd.add("ipv4.ignore-auto-dns");
            cmd.add(dns.isBlank() ? "no" : "yes");
            runWrite(cmd.toArray(String[]::new));
        } else {
            runWrite("nmcli", "con", "modify", con,
                    "ipv4.method", "auto",
                    "ipv4.addresses", "",
                    "ipv4.gateway", "",
                    "ipv4.dns", "",
                    "ipv4.ignore-auto-dns", "no");
        }
        runWrite("nmcli", "con", "up", con);
        IODeviceDTO io = new IODeviceDTO();
        io.setDevice(iface);
        return getNetwork(io);
    }

    Object setNetworkInterface(IODeviceDTO io) throws Exception {
        if (io == null || io.getDevice() == null || io.getDevice().isBlank()) {
            throw new IllegalArgumentException("device is required");
        }
        String iface = io.getDevice();
        // Safe migration behavior: select/activate target interface only; do not disable other interfaces.
        try {
            runWrite("nmcli", "device", "connect", iface);
        } catch (Exception ignored) {
            // fallback to lightweight link-up, keep non-destructive
            try {
                runWrite("ip", "link", "set", iface, "up");
            } catch (Exception ignored2) {
            }
        }
        return getNetwork(io);
    }

    Object addDefaultRoute(IODeviceDTO io) throws Exception {
        if (io == null || io.getDevice() == null || io.getDevice().isBlank()) {
            throw new IllegalArgumentException("device is required");
        }
        String iface = io.getDevice();
        String con = ensureConnectionName(iface);
        try {
            runWrite("nmcli", "con", "modify", con, "ipv4.never-default", "no");
            runWrite("nmcli", "con", "modify", con, "ipv4.route-metric", "1");
            runWrite("nmcli", "con", "up", con);
            return null;
        } catch (Exception nmErr) {
            String gateway = getDefaultGateway(iface);
            if (gateway == null || gateway.isBlank()) {
                throw nmErr;
            }
            runWrite("ip", "route", "replace", "default", "via", gateway, "dev", iface, "metric", "1");
            return null;
        }
    }

    NetworkIPMacDTO getIp(IODeviceDTO io, boolean vpn) throws Exception {
        if (io == null || io.getDevice() == null || io.getDevice().isBlank()) {
            throw new IllegalArgumentException("Network interface not selected");
        }
        NetworkIPMacDTO dto = new NetworkIPMacDTO();
        dto.setMac(readTrim("/sys/class/net/" + io.getDevice() + "/address"));
        dto.setIp(vpn ? getIpv4Only("wg0") : getIpv4Only(io.getDevice()));
        return dto;
    }

    private boolean isUserFacingNetIf(String iface) {
        return (iface.startsWith("e") || iface.startsWith("w")) && !iface.startsWith("wg0");
    }

    private boolean matchesType(String type, String iface) {
        if (type == null) {
            return true;
        }
        String t = type.trim().toLowerCase();
        if ("lan".equals(t)) {
            return iface.startsWith("e");
        }
        if ("wi-fi".equals(t) || "wifi".equals(t)) {
            return iface.startsWith("w") && !iface.startsWith("wg0");
        }
        return true;
    }

    private String getDefaultRouteInterface() throws Exception {
        for (String line : run("ip", "route", "show", "default")) {
            String[] parts = line.trim().split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("dev".equals(parts[i])) {
                    return parts[i + 1];
                }
            }
        }
        return null;
    }

    private String getDefaultGateway(String iface) throws Exception {
        for (String line : run("ip", "route", "show", "default")) {
            String[] parts = line.trim().split("\\s+");
            String dev = null;
            String via = null;
            for (int i = 0; i < parts.length - 1; i++) {
                if ("dev".equals(parts[i])) dev = parts[i + 1];
                if ("via".equals(parts[i])) via = parts[i + 1];
            }
            if (iface.equals(dev)) {
                return via;
            }
        }
        return null;
    }

    private String getIpv4Only(String iface) throws Exception {
        IpAndMask v = getIpv4AndNetmask(iface);
        return v.ip;
    }

    private IpAndMask getIpv4AndNetmask(String iface) throws Exception {
        List<String> lines = run("ip", "-o", "-4", "addr", "show", "dev", iface);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("inet".equals(parts[i])) {
                    String cidr = parts[i + 1];
                    String[] split = cidr.split("/");
                    if (split.length == 2) {
                        IpAndMask v = new IpAndMask();
                        v.ip = split[0];
                        v.netmask = prefixToMask(Integer.parseInt(split[1]));
                        return v;
                    }
                }
            }
        }
        return new IpAndMask();
    }

    private String prefixToMask(int prefix) {
        int mask = prefix == 0 ? 0 : 0xffffffff << (32 - prefix);
        int value = mask;
        return ((value >>> 24) & 0xff) + "." + ((value >>> 16) & 0xff) + "." + ((value >>> 8) & 0xff) + "." + (value & 0xff);
    }

    private List<String> getDnsServers() {
        List<String> dns = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(Path.of("/etc/resolv.conf"), StandardCharsets.UTF_8)) {
                String s = line.trim();
                if (s.startsWith("#") || s.startsWith("domain") || s.isBlank()) {
                    continue;
                }
                if (s.startsWith("nameserver")) {
                    String[] parts = s.split("\\s+");
                    if (parts.length > 1) {
                        dns.add(parts[1]);
                    }
                }
                if (dns.size() >= 2) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return dns;
    }

    private boolean isStaticIp(String iface) {
        try {
            List<String> nm = run("nmcli", "-t", "-g", "GENERAL.CONNECTION", "dev", "show", iface);
            if (!nm.isEmpty()) {
                String connection = nm.get(0).trim();
                if (!connection.isBlank() && !"--".equals(connection)) {
                    List<String> method = run("nmcli", "-t", "-g", "ipv4.method", "con", "show", connection);
                    if (!method.isEmpty()) {
                        return "manual".equalsIgnoreCase(method.get(0).trim());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String readTrim(String path) {
        try {
            return Files.readString(Path.of(path)).trim();
        } catch (Exception e) {
            return null;
        }
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
            while ((line = r.readLine()) != null) {
                out.add(line);
            }
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

    private String ensureConnectionName(String iface) throws Exception {
        List<String> out = run("nmcli", "-t", "--escape", "no", "-g", "GENERAL.CONNECTION", "dev", "show", iface);
        if (!out.isEmpty()) {
            String con = out.get(0).trim();
            if (!con.isBlank() && !"--".equals(con)) {
                return con;
            }
        }
        // Try to bring interface up to let NetworkManager attach/create a profile.
        runWrite("nmcli", "device", "connect", iface);
        out = run("nmcli", "-t", "--escape", "no", "-g", "GENERAL.CONNECTION", "dev", "show", iface);
        if (!out.isEmpty()) {
            String con = out.get(0).trim();
            if (!con.isBlank() && !"--".equals(con)) {
                return con;
            }
        }
        throw new IllegalStateException("No active NetworkManager connection for " + iface);
    }

    private int maskToPrefix(String mask) {
        String[] parts = mask.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid netmask: " + mask);
        }
        int prefix = 0;
        for (String part : parts) {
            int b = Integer.parseInt(part);
            prefix += Integer.bitCount(b & 0xff);
        }
        return prefix;
    }

    private String joinDns(String dns1, String dns2) {
        List<String> dns = new ArrayList<>();
        if (dns1 != null && !dns1.isBlank()) dns.add(dns1.trim());
        if (dns2 != null && !dns2.isBlank()) dns.add(dns2.trim());
        return String.join(",", dns);
    }

    private static final class IpAndMask {
        String ip;
        String netmask;
    }
}
