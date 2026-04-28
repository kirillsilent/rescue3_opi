package kz.idc.utils.gpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.plugin.linuxfs.provider.gpio.digital.LinuxFsDigitalOutputProvider;
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform;
import io.micronaut.context.annotation.Requires;
import javax.inject.Singleton;

@Singleton
@Requires(property = "gpio.enabled", value = "true")
public class GPIO {

    private static Context pi4j;
    private static DigitalOutput led;

    public GPIO() {
        // Pi4J v2: linuxfs (/sys/class/gpio) — без нативной libgpiod; для доступа к GPIO пользователь pi должен быть в группе gpio
        pi4j = Pi4J.newContextBuilder()
                .add(new RaspberryPiPlatform())
                .add(LinuxFsDigitalOutputProvider.newInstance())
                .build();
        init();
    }

    private void init(){
        initLight();
    }

    private void initLight(){
        // LED на GPIO4 (BCM 4) и Ground. На ядре с gpiochip512 глобальный номер = 512 + 4 = 516
        int ledPin = 516;
        led = pi4j.dout().create(ledPin);
    }

    public static void shutdown() {
        if (pi4j != null) {
            pi4j.shutdown();
        }
    }

    public static void stopLight() {
        if (led != null) {
            led.low();
        }
    }

    public static void startLight() {
        if (led != null) {
            led.high();
        }
    }

    // GPIO input events are handled by the AI emergency service (voice recognition),
    // not by this Java service.
}
