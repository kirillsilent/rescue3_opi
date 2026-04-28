package kz.idc.rs.services.rescue;

import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Maybe;

public interface Streams {
    Maybe<StreamedFile> getPlanStream();
}
