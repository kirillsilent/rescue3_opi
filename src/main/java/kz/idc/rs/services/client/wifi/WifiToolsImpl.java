package kz.idc.rs.services.client.wifi;

import io.reactivex.Maybe;
import kz.idc.dto.io.IOType;
import kz.idc.dto.wifi.*;
import kz.idc.error.$Error;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;


public class WifiToolsImpl implements WifiTools {

    private final Storage storage = $Storage.mk();
    private final WifiToolsLocalProbe localProbe = new WifiToolsLocalProbe();

    public WifiToolsImpl() {
    }

    @Override
    public Maybe<Object> getPoint() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.getPoint(resolveWifiIface(io.getDevice())))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));
    }

    @Override
    public Maybe<Object> getPoints() {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.getPoints(resolveWifiIface(io.getDevice())))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));
    }

    @Override
    public Maybe<Object> getConnection(String point) {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.getConnection(resolveWifiIface(io.getDevice()), point))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));
    }

    @Override
    public Maybe<Object> connect(ConnectToPointDTO connectToPointDTO) {
        return storage.getIO(IOType.NETWORK.DEVICE).toMaybe()
                .flatMap(io -> Maybe.create(result -> result.onSuccess(localProbe.connect(resolveWifiIface(io.getDevice()), connectToPointDTO))))
                .onErrorResumeNext(t -> result -> result.onSuccess($Error.mk().createErrorNetworkModule(t.getMessage())));
    }

    private String resolveWifiIface(String currentIface) {
        if (currentIface != null && currentIface.startsWith("w")) {
            return currentIface;
        }
        String detected = localProbe.findFirstWifiInterface();
        return detected != null ? detected : currentIface;
    }
}
