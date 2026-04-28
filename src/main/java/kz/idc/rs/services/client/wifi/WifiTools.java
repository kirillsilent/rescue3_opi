package kz.idc.rs.services.client.wifi;

import io.reactivex.Maybe;
import kz.idc.dto.wifi.ConnectToPointDTO;


public interface WifiTools {
    Maybe<Object> getPoint();
    Maybe<Object> getPoints();
    Maybe<Object> getConnection(String wifiAp);
    Maybe<Object> connect(ConnectToPointDTO connectToPointDTO);
}
