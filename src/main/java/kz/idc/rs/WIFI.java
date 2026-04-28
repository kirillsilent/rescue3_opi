package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.dto.wifi.ConnectToPointDTO;
import kz.idc.rs.services.client.ClientAPIService;
import kz.idc.rs.services.client.wifi.WifiTools;

@Controller(value = "/wifi")
public class WIFI {

    private final WifiTools wifiTools;

    public WIFI(RxHttpClient rxHttpClient) {
        this.wifiTools = ClientAPIService.wifi(rxHttpClient);
    }

    @Get("/get_point")
    public Maybe<Object> getPoint() {
        return wifiTools.getPoint();
    }

    @Get("/get_points")
    public Maybe<Object> getPoints() {
        return wifiTools.getPoints();
    }

    @Get("/get_connection")
    public Maybe<Object> getConnection(@QueryValue String point) {
        return wifiTools.getConnection(point);
    }

    @Post("/connect")
    public Maybe<Object> connect(ConnectToPointDTO connectToPointDTO) {
        return wifiTools.connect(connectToPointDTO);
    }
}
