package kz.idc.rs.services.rescue;

import io.micronaut.http.MediaType;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Maybe;
import kz.idc.utils.file.$FileUtils;

import java.io.InputStream;

public class StreamsImpl implements Streams{
    @Override
    public Maybe<StreamedFile> getPlanStream() {
        return Maybe.create(stream -> {
            InputStream is = $FileUtils.mk().getPlan();
            StreamedFile streamedFile = new StreamedFile(is, MediaType.ALL_TYPE);
            stream.onSuccess(streamedFile);
        });
    }
}
