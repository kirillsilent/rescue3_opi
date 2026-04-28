package kz.idc.rs.client.card.requests;

import io.micronaut.http.HttpRequest;
import kz.idc.dto.IncidentDTO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiCardRequestsImpl implements ApiCardRequests {

    private final ApiCardConfig apiCardConfig;

    @Override
    public HttpRequest<?> incident(IncidentDTO incidentDTO) {
        return HttpRequest.POST(apiCardConfig.incident(), incidentDTO);
    }
}
