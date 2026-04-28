package kz.idc.rs;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.reactivex.Maybe;
import kz.idc.dto.IncidentDTO;
import kz.idc.utils.storage.$Storage;
import kz.idc.utils.storage.Storage;

@Controller("/incident_settings")
public class IncidentSettingsAPI {

    private final Storage storage = $Storage.mk();

    @Get("/get")
    public Maybe<IncidentDTO> getIncidentSettings() {
        return storage.getIncident().toMaybe();
    }

    @Put("/set")
    public Maybe<IncidentDTO> setIncidentSettings(IncidentDTO incidentDTO) {
        return storage.setIncident(incidentDTO).toMaybe();
    }
}

