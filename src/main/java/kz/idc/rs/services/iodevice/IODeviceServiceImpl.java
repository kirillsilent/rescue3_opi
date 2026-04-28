package kz.idc.rs.services.iodevice;

import io.reactivex.*;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;

import java.util.List;


public class IODeviceServiceImpl implements IODeviceService {

    private final Storage storage = $Storage.mk();

    @Override
    public Single<IODeviceDTO> getHardware(String hardwareType) {
        return storage.getSettings().flatMap(settingsDTO -> Single.create(e -> {
            for (IODeviceDTO ioDevice :
                    settingsDTO.getIo()) {
                if (ioDevice.getType().equals(hardwareType)) {
                    e.onSuccess(ioDevice);
                    break;
                }
            }
        }));
    }

    @Override
    public Single<List<IODeviceDTO>> getHardwares() {
        return storage.getSettings()
                .flatMap(settingsDTO -> io -> io.onSuccess(settingsDTO.getIo()));
    }

    @Override
    public Single<IODeviceDTO> setHardware(IODeviceDTO io) {
        return storage.setIO(io);
    }
}
