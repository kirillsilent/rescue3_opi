package kz.idc.rs.services.client.ac;

import kz.idc.dto.VolumeDTO;
import kz.idc.dto.io.IODeviceAvailableDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IOType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class ACToolsLocalProbe {

    private static final Pattern ALSA_CARD_LINE = Pattern.compile("card\\s+(\\d+):\\s*([^\\[]+)\\[[^\\]]*],\\s*device\\s+(\\d+):");
    private static final Pattern AMIXER_PERCENT = Pattern.compile("\\[(\\d{1,3})%]");
    private static final Duration CMD_TIMEOUT = Duration.ofSeconds(2);
    private final Object playerLock = new Object();
    private volatile boolean playerRunning;
    private volatile Process playerProcess;
    private volatile Thread playerThread;

    List<IODeviceDTO> audioCards(String type) throws Exception {
        List<AlsaCardEntry> entries = parseAlsaCards(type);
        List<IODeviceDTO> devices = new ArrayList<>();
        for (AlsaCardEntry entry : entries) {
            String name = entry.cardName;
            if (name.isEmpty()) {
                continue;
            }
            IODeviceDTO dto = new IODeviceDTO();
            dto.setType(type);
            dto.setDevice(name);
            if (devices.stream().noneMatch(d -> name.equals(d.getDevice()))) {
                devices.add(dto);
            }
        }
        if (devices.isEmpty()) {
            return devices;
        }
        boolean fullList = true;
        if (fullList) {
            List<IODeviceDTO> usb = devices.stream()
                    .filter(d -> d.getDevice() != null && d.getDevice().toLowerCase().contains("usb"))
                    .collect(Collectors.toList());
            if (!usb.isEmpty()) {
                return new ArrayList<>(usb);
            }
        }
        return devices;
    }

    VolumeDTO getVolume(IODeviceDTO ioDeviceDTO) throws Exception {
        AlsaCardEntry entry = resolveAudioEntry(ioDeviceDTO);
        for (String mixer : mixersForType(ioDeviceDTO.getType())) {
            try {
                int value = readAmixerPercent(entry.cardIndex, mixer);
                VolumeDTO dto = new VolumeDTO();
                dto.setVolume(value);
                return dto;
            } catch (Exception ignored) {
                // try next mixer like python ac_tools
            }
        }
        VolumeDTO dto = new VolumeDTO();
        dto.setVolume(0);
        return dto;
    }

    void setVolume(IODeviceDTO ioDeviceDTO, int volume) throws Exception {
        AlsaCardEntry entry = resolveAudioEntry(ioDeviceDTO);
        Exception last = null;
        for (String mixer : mixersForType(ioDeviceDTO.getType())) {
            try {
                run("amixer", "-c", String.valueOf(entry.cardIndex), "set", mixer, Math.max(0, Math.min(100, volume)) + "%");
                return;
            } catch (Exception e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
        throw new IllegalStateException("No mixer available for " + ioDeviceDTO.getType());
    }

    void play(IODeviceDTO ioDeviceDTO, String track) throws Exception {
        if (track == null || track.isBlank()) {
            throw new IllegalArgumentException("Track path is empty");
        }
        AlsaCardEntry entry = resolveAudioEntry(ioDeviceDTO);
        synchronized (playerLock) {
            stopPlaybackInternal();
            playerRunning = true;
            playerThread = new Thread(() -> playbackLoop(entry, track), "ac-tools-player");
            playerThread.setDaemon(true);
            playerThread.start();
        }
    }

    void stopPlayback() {
        synchronized (playerLock) {
            stopPlaybackInternal();
        }
    }

    List<IODeviceDTO> cameras() throws Exception {
        List<String> lines = run("v4l2-ctl", "--list-devices");
        Map<String, List<String>> cameraNodes = new LinkedHashMap<>();
        String currentName = null;
        for (String line : lines) {
            if (line.contains(":") && !line.startsWith("\t") && !line.startsWith(" ")) {
                String name = line.substring(0, line.lastIndexOf(':')).trim();
                if (isExcludedCameraHeader(line)) {
                    currentName = null;
                    continue;
                }
                currentName = name;
                cameraNodes.putIfAbsent(currentName, new ArrayList<>());
                continue;
            }
            if (currentName != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("/dev/video")) {
                    cameraNodes.get(currentName).add(trimmed);
                }
            }
        }
        List<IODeviceDTO> devices = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : cameraNodes.entrySet()) {
            if (isHiddenCameraNode(entry.getKey()) || entry.getValue().isEmpty()) {
                continue;
            }
            IODeviceDTO dto = new IODeviceDTO();
            dto.setType(IOType.CAMERA.DEVICE);
            dto.setDevice(entry.getValue().get(0));
            devices.add(dto);
        }
        devices.sort(Comparator.comparing(IODeviceDTO::getDevice, Comparator.nullsLast(String::compareTo)));
        return devices;
    }

    IODeviceAvailableDTO isAvailable(String type, IODeviceDTO selected) throws Exception {
        if (selected == null || selected.getDevice() == null) {
            return IODeviceAvailableDTO.create(type, false);
        }
        List<IODeviceDTO> devices = IOType.CAMERA.DEVICE.equals(type) ? cameras() : audioCards(type);
        boolean ok = devices.stream().anyMatch(d -> selected.getDevice().equals(d.getDevice()));
        return IODeviceAvailableDTO.create(type, ok);
    }

    private boolean isExcludedCameraHeader(String line) {
        String l = line.toLowerCase();
        return l.contains("vchiq:") || l.contains("bcm2835-codec") || l.contains("rpi-hevc-dec");
    }

    private boolean isHiddenCameraNode(String name) {
        if (name == null) {
            return false;
        }
        String l = name.toLowerCase();
        return l.contains("unicam") || l.contains("bcm2835-isp");
    }

    private List<String> run(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(CMD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("Timeout running " + command[0]);
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(command[0] + " exited with " + process.exitValue());
        }
        return lines;
    }

    private void playbackLoop(AlsaCardEntry entry, String track) {
        while (playerRunning) {
            Process process = null;
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "aplay",
                        "-D", "hw:" + entry.cardIndex + "," + entry.deviceIndex,
                        track
                );
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = pb.start();
                playerProcess = process;
                int code = process.waitFor();
                if (!playerRunning) {
                    return;
                }
                if (code != 0) {
                    playerRunning = false;
                    return;
                }
                // Python version repeats playback indefinitely until stop().
            } catch (Exception e) {
                playerRunning = false;
                return;
            } finally {
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
                playerProcess = null;
            }
        }
    }

    private List<AlsaCardEntry> parseAlsaCards(String type) throws Exception {
        String tool = IOType.AUDIO_INPUT.DEVICE.equals(type) ? "arecord" : "aplay";
        List<String> lines = run(tool, "-l");
        List<AlsaCardEntry> entries = new ArrayList<>();
        for (String line : lines) {
            Matcher m = ALSA_CARD_LINE.matcher(line);
            if (!m.find()) {
                continue;
            }
            AlsaCardEntry entry = new AlsaCardEntry();
            entry.cardIndex = Integer.parseInt(m.group(1));
            entry.cardName = m.group(2).trim();
            entry.deviceIndex = Integer.parseInt(m.group(3));
            entries.add(entry);
        }
        return entries;
    }

    private AlsaCardEntry resolveAudioEntry(IODeviceDTO ioDeviceDTO) throws Exception {
        List<AlsaCardEntry> entries = parseAlsaCards(ioDeviceDTO.getType());
        String selected = ioDeviceDTO.getDevice();
        if (selected != null) {
            for (AlsaCardEntry entry : entries) {
                if (entry.cardName.equals(selected)) {
                    return entry;
                }
            }
            for (AlsaCardEntry entry : entries) {
                if (entry.rawLineContains(selected)) {
                    return entry;
                }
            }
        }
        if (!entries.isEmpty()) {
            return entries.get(0);
        }
        throw new IllegalStateException("No ALSA cards for type " + ioDeviceDTO.getType());
    }

    private int readAmixerPercent(int cardIndex, String mixer) throws Exception {
        List<String> lines = run("amixer", "-c", String.valueOf(cardIndex), "get", mixer);
        for (String line : lines) {
            Matcher m = AMIXER_PERCENT.matcher(line);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }
        throw new IllegalStateException("No percent value in amixer output for mixer " + mixer);
    }

    private List<String> mixersForType(String type) {
        if (IOType.AUDIO_INPUT.DEVICE.equals(type)) {
            return List.of("Mic");
        }
        return List.of("PCM", "Speaker", "Headphone", "HDMI");
    }

    private void stopPlaybackInternal() {
        playerRunning = false;
        Process process = playerProcess;
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
        Thread thread = playerThread;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        playerProcess = null;
        playerThread = null;
    }

    private static final class AlsaCardEntry {
        int cardIndex;
        int deviceIndex;
        String cardName;

        boolean rawLineContains(String text) {
            return cardName != null && text != null && cardName.contains(text);
        }
    }
}
