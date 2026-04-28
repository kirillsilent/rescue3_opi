package kz.idc.rs.services.client.network;

import io.micronaut.http.HttpResponse;
import io.reactivex.*;
import kz.idc.dto.StatusDTO;
import kz.idc.dto.io.IODeviceDTO;
import kz.idc.dto.io.IOType;
import kz.idc.dto.network.*;
import kz.idc.error.$Error;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkToolsImpl implements NetworkTools {
    private static final Logger log = LoggerFactory.getLogger(NetworkToolsImpl.class);

    private final Storage storage = $Storage.mk();
    private final NetworkToolsLocalProbe localProbe = new NetworkToolsLocalProbe();

    public NetworkToolsImpl() {
    }

    @Override
    public Maybe<Object> getNetworkInterfaces(String type) {
        try {
            return Maybe.create(result -> result.onSuccess(localProbe.getNetworkInterfaces(type)));
        } catch (Exception e) {
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<Object> setNetworkConfig(NetworkConfigDTO networkConfigDTO) {
        try {
            Object resp = localProbe.setNetworkConfig(networkConfigDTO);
            return Maybe.create(result -> result.onSuccess(resp));
        } catch (Exception e) {
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<Object> setNetworkInterface(IODeviceDTO io) {
        try {
            IODeviceDTO normalized = io == null ? new IODeviceDTO() : io;
            if (normalized.getType() == null || normalized.getType().isBlank()) {
                normalized.setType(IOType.NETWORK.DEVICE);
            }
            Object network = localProbe.setNetworkInterface(normalized);
            return storage.setIO(normalized).toMaybe()
                    .flatMap(iface -> result -> result.onSuccess(network));
        } catch (Exception e) {
            return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
        }
    }

    @Override
    public Maybe<Object> getCurrentNetworkInterface() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.getNetwork(io))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));
    }

    @Override
    public Maybe<Object> getStatusNetworkInterface() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> {
                    NetworkConfigDTO cfg = localProbe.getNetwork(io);
                    return Maybe.create(result -> result.onSuccess(StatusDTO.create(cfg.getIp() != null)));
                })
                .onErrorResumeNext(t -> result -> result.onSuccess(StatusDTO.create(false)));
    }

    @Override
    public Maybe<Object> isDefaultRoute() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.isDefaultRoute(io))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));

    }

    @Override
    public Maybe<Object> addDefaultRoute() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> {
                    try {
                        localProbe.addDefaultRoute(io);
                        return Maybe.create(result -> result.onSuccess(HttpResponse.ok()));
                    } catch (Exception e) {
                        return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
                    }
                });
    }

    @Override
    public Maybe<Object> getIpFromInterface(boolean isVPNEnabled) {
        Maybe<Object> maybe =  storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> get -> {
                    if (io.getDevice() == null) {
                        get.onError(new Throwable());
                    } else {
                        get.onSuccess(io);
                    }
                });
        if(!isVPNEnabled){
            return maybe.flatMap(io -> {
                try {
                    return Maybe.create(result -> result.onSuccess(localProbe.getIp((IODeviceDTO) io, false)));
                } catch (Exception e) {
                    return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
                }
            });
        }else {
            return maybe.flatMap(io -> {
                try {
                    return Maybe.create(result -> result.onSuccess(localProbe.getIp((IODeviceDTO) io, true)));
                } catch (Exception e) {
                    return Maybe.create(result -> result.onSuccess($Error.mk().createErrorNetworkModule(e.getMessage())));
                }
            });
        }
    }

    @Override
    public Maybe<VPNEnabledDTO> setWorkOnVPN(VPNEnabledDTO vpnEnabledDTO) {
        return storage.setEnabledVPN(vpnEnabledDTO).toMaybe();
    }

    @Override
    public Maybe<VPNEnabledDTO> getStateWorkOnVPN() {
        return storage.getEnabledVPN().toMaybe();
    }
}
