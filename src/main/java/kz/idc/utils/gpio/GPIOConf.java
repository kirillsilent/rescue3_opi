package kz.idc.utils.gpio;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import java.util.List;

@ConfigurationProperties(GPIOConf.PREFIX)
@Requires(property = GPIOConf.PREFIX)
public class GPIOConf {
    public static final String PREFIX = "gpio";  // Ожидает gpio в корне

    private List<Integer> valuesForRead;

    public List<Integer> getValuesForRead() {
        return valuesForRead;
    }

    public void setValuesForRead(List<Integer> valuesForRead) {
        this.valuesForRead = valuesForRead;
    }
}


