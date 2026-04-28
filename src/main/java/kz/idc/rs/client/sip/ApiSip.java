package kz.idc.rs.client.sip;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;

public interface ApiSip {
    void started();
    Maybe<HttpResponse<HttpStatus>> call();
    void end();
    Disposable error();
}
