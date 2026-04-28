package kz.idc.rs.services.iodevice;


public class $IODeviceService {

    private static IODeviceServiceImpl mIODeviceImpl;

    public static IODeviceService mk() {
        if (mIODeviceImpl == null) {
            mIODeviceImpl = new IODeviceServiceImpl();
        }
        return mIODeviceImpl;
    }
}
