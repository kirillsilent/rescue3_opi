package kz.idc;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.Micronaut;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IOType;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.ac.ACTools;
import kz.idc.utils.gpio.GPIO;
import kz.idc.utils.gpio.GPIOStates;
import kz.idc.utils.storage.Storage;
import kz.idc.utils.storage.$Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

@Context
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final int AUDIO_RESTORE_ATTEMPTS = 12;
    private static final long AUDIO_RESTORE_DELAY_MS = 2_000L;

    private static class Hook extends Thread {
        public void run() {
            GPIO.stopLight();
            GPIO.shutdown();
            log.info(GPIOStates.SHUTDOWN.STATE);
        }
    }

    public static void main(String[] args) {
        Storage storage = $Storage.mk();
        storage.createStorage();
        restoreAudioVolumesAsync(storage);
        Micronaut.build(args)
                .eagerInitSingletons(true)
                .mainClass(Application.class)
                .start();
        Runtime.getRuntime().addShutdownHook(new Hook());
    }

    @PostConstruct
    void init(@Value("${card.host:default-value}") String cardApi,
            @Value("${gpio.rf433:default-value}") List<Integer> values)  {
        log.info("card api host is: {}", cardApi);
        log.info("Values for read from rf433: {}", values.toString());
    }

    private static void restoreAudioVolumesAsync(Storage storage) {
        Thread thread = new Thread(() -> {
            ACTools acTools = ClientAPIService.ac();
            restoreVolumeForTypeWithRetry(storage, acTools, IOType.AUDIO_INPUT.DEVICE);
            restoreVolumeForTypeWithRetry(storage, acTools, IOType.AUDIO_OUTPUT.DEVICE);
        }, "audio-volume-restore");
        thread.setDaemon(true);
        thread.start();
    }

    private static void restoreVolumeForTypeWithRetry(Storage storage, ACTools acTools, String type) {
        for (int attempt = 1; attempt <= AUDIO_RESTORE_ATTEMPTS; attempt++) {
            try {
                IODeviceDTO io = storage.getIO(type).blockingGet();
                if (io == null || io.getDevice() == null || io.getVolume() == null) {
                    return;
                }
                acTools.setVolume(io, io.getVolume()).blockingGet();
                log.info("Restored {} volume to {} on attempt {}", type, io.getVolume(), attempt);
                return;
            } catch (Exception e) {
                if (attempt == AUDIO_RESTORE_ATTEMPTS) {
                    log.warn("Failed to restore {} volume after {} attempts: {}", type, attempt, e.getMessage());
                    return;
                }
                log.info("Audio device for {} is not ready yet, retrying restore attempt {} of {}", type, attempt, AUDIO_RESTORE_ATTEMPTS);
                try {
                    Thread.sleep(AUDIO_RESTORE_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
