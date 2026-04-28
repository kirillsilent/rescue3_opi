package kz.idc.rs.client.card.requests;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.uri.UriBuilder;

import java.io.File;
import java.net.URI;

import javax.inject.Singleton;
@Singleton
@ConfigurationProperties(ApiCardConfig.PREFIX)
@Requires(property = ApiCardConfig.PREFIX)
public class ApiCardConfig {

    public static final String PREFIX = "card";

    @Property(name = "card.host")
    private String host;

    public URI incident() {

        String path = "incident";

        return UriBuilder.of(host)
                .path(File.separator)
                .path(path)
                .build();
    }

}