package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.Maybe;
import kz.idc.dto.IsSettingsSetDTO;
import kz.idc.rs.services.settings.$SettingsService;

@Controller("/settings")
public class MainSettings {

    @Get("/is_set")
    public Maybe<IsSettingsSetDTO> getSettings(){
        return $SettingsService.mk().isSettingsSet();
    }

}
