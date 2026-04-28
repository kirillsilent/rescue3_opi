package kz.idc.rs.services.iodevice;

import io.reactivex.Single;
import kz.idc.dto.io.IODeviceDTO;

import java.util.List;

public interface IODeviceService {
    Single<IODeviceDTO> getHardware(String hardwareType);
    Single<List<IODeviceDTO>> getHardwares();
    Single<IODeviceDTO> setHardware(IODeviceDTO ioDeviceDTO);
}
