package kz.idc.rs.client.card.requests;

import io.micronaut.http.HttpRequest;
import kz.idc.dto.IncidentDTO;

public interface ApiCardRequests {
    HttpRequest<?> incident(IncidentDTO incidentDTO);
}
