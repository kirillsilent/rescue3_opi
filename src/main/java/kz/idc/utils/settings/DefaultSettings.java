package kz.idc.utils.settings;

import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IOType;
import kz.idc.dto.ServerAddressDTO;
import kz.idc.dto.sip.SipDTO;
import kz.idc.dto.sip.acc.AccountDTO;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DefaultSettings {

    //WebHost
    private static final String webHost = "http://10.18.0.61";
    private static final int webPort = 80;

    //Sip config
    private static final String sipHost = "10.18.0.61";
    private static final int sipPort = 5060;
    private static final String serverAccount = "http://10.18.0.61";
    private static final int serverAccountPort = 7000;
    private static final String operator = "10000";


    public static boolean vpn(){
        return false;
    }

    public static ServerAddressDTO webServer(){
        ServerAddressDTO serverConfigDTO = new ServerAddressDTO();
        serverConfigDTO.setHostname(webHost);
        serverConfigDTO.setPort(webPort);
        return serverConfigDTO;
    }

    public static SipDTO sip(){
        SipDTO sipDTO = new SipDTO();
        sipDTO.setHostname(sipHost);
        sipDTO.setPort(sipPort);
        sipDTO.setOperator(operator);
        sipDTO.setAccount(AccountDTO.create("",""));
        sipDTO.setSipRegServer(ServerAddressDTO.create(serverAccount, serverAccountPort));
        return sipDTO;
    }

    public static List<IODeviceDTO> ioDevices(){
        List<IODeviceDTO> ioDeviceDTOS = new ArrayList<>();
        ioDeviceDTOS.add(createIODevice(IOType.AUDIO_INPUT.DEVICE));
        ioDeviceDTOS.add(createIODevice(IOType.AUDIO_OUTPUT.DEVICE));
        ioDeviceDTOS.add(createIODevice(IOType.CAMERA.DEVICE));
        ioDeviceDTOS.add(createIODevice(IOType.NETWORK.DEVICE));
        return ioDeviceDTOS;
    }

    private static IODeviceDTO createIODevice(String type){
        IODeviceDTO ioDeviceDTO = new IODeviceDTO();
        ioDeviceDTO.setType(type);
        return ioDeviceDTO;
    }
}
