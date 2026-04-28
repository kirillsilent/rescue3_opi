package kz.idc.rs.services.client.api.requests;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.uri.UriBuilder;
import kz.idc.dto.rescue.RescueDTO;
import kz.idc.dto.sip.acc.AccountDTO;

import java.io.File;
import java.net.URI;

@ConfigurationProperties(APIConfiguration.PREFIX)
@Requires(property = APIConfiguration.PREFIX)
public class APIConfiguration {
    public static final String PREFIX = "api";

    URI createRegRescuePath(String address) {

        String path = "/api/terminals";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .build();
    }

    URI getAudiosPath(String address) {

        String path = "/api/terminals/emergencies/audios";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .build();
    }

    URI getPlanPath(String address, RescueDTO rescueDTO) {

        String path = "/api/terminals/evacuation_image_plan";
        String id = "terminal_id";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .queryParam(id, rescueDTO.getId())
                .build();
    }

    URI getMarquee(String address) {

        String path = "/api/terminals/marquees";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .build();
    }

    URI setMarqueeStatus(long id,String address) {

        String path = "/api/terminals/marquee/status";
        String param = "terminal_id";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .queryParam(param, id)
                .build();
    }

    URI downloadFile(String address, String path, String fileName) {
        return UriBuilder.of(address)
                .path(path)
                .path(fileName)
                .build();
    }

    URI updateStatusDownloadBackend(String address, RescueDTO rescueDTO) {

        String path = "/api/terminals/downloads";
        String id = "terminal_id";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .queryParam(id, rescueDTO.getId())
                .build();
    }

    URI createRegSipId(String address) {
        String path = "user/create";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .build();
    }

    public URI updateRegSipId(String address, AccountDTO sipAccount) {

        String path = "user/update";
        String account = "account";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .queryParam(account, sipAccount.getAccount())
                .build();
    }

    public URI createEmergency(String address) {

        String path = "/api/emergencies";

        return UriBuilder.of(address)
                .path(File.separator)
                .path(path)
                .build();
    }

}