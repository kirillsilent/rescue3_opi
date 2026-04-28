package kz.idc.rs.client.sip;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import kz.idc.Application;
import kz.idc.dto.IncidentDTO;
import kz.idc.error.$Error;
import kz.idc.rs.services.client.sip.$SipClient;
import kz.idc.rs.services.client.sip.SipClient;
import kz.idc.utils.dates.$DateUtils;
import kz.idc.utils.storage.$Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ApiSipImpl implements ApiSip {

    private boolean isCalling = false;

    private final Logger log = LoggerFactory.getLogger(Application.class);
    private final RxHttpClient rxHttpClient;
    private final SipClient sipClient;

    public ApiSipImpl(RxHttpClient rxHttpClient) {
        this.rxHttpClient = rxHttpClient;
        this.sipClient = $SipClient.mk(rxHttpClient);
    }

    @Override
    public void started() {
        log.info(SipStatus.SIP_STARTED.STATUS);
    }

    @Override
    public Maybe<HttpResponse<HttpStatus>> call() {
        if (!isCalling) {
            final Date date = new Date();
            log.info(SipStatus.CALLING.STATUS);
            isCalling = true;
            postEventIncidentAsync(date);
            return sipClient.call()
                    .doOnTerminate(() -> {
                        // Логируем завершение вызова
                        log.info(SipStatus.CALL_END.STATUS);
                        isCalling = false; // Сбрасываем флаг isCalling после завершения
                    })
                    .onErrorResumeNext(t -> {
                        isCalling = false;
                        log.error($Error.mk().translateSip(t.getMessage()));
                        return Maybe.just(HttpResponse.serverError());
                    });
        } else {
            log.warn(SipStatus.ALREADY_CALLING.STATUS);
            // Вместо flatMapSingle используем Maybe.defer для задержки и повторного вызова
            return Maybe.defer(() -> {
                // Задержка перед повторной попыткой
                return Maybe.timer(2, TimeUnit.SECONDS)
                        .flatMap(m -> call()); // Рекурсивно повторяем вызов
            });
        }
    }

    /**
     * Отправка JSON события на настроенный "Адрес сервера событий".
     * URL в тело incident не входит.
     */
    private void postEventIncidentAsync(Date date) {
        $Storage.mk().getEventServerUrl()
                .flatMap(url -> {
                    if (url == null || url.isBlank()) {
                        return Single.just(false);
                    }
                    return $Storage.mk().getIncident()
                            .flatMap(incident -> postIncidentToUrl(url, incident, date));
                })
                .subscribe(
                        ok -> {
                            if (!ok) {
                                log.debug("Event server post skipped/failed");
                            }
                        },
                        t -> log.warn("Event server post failed: {}", t.getMessage())
                );
    }

    private Single<Boolean> postIncidentToUrl(String url, IncidentDTO incident, Date date) {
        try {
            incident.setDate_time($DateUtils.mk().createDateForAPI(date));
            return rxHttpClient.exchange(HttpRequest.POST(URI.create(url), incident), Object.class)
                    .firstOrError()
                    .map(resp -> true)
                    .onErrorReturnItem(false);
        } catch (Exception ex) {
            return Single.just(false);
        }
    }

    @Override
    public void end() {
        isCalling = false;
        log.info(SipStatus.CALL_END.STATUS);
    }

    @Override
    public Disposable error() {
        isCalling = false;
        log.info(SipStatus.SIP_ERROR.STATUS);
        return Single.create((SingleEmitter<String> e) -> {
            sipClient.restart().blockingGet();
            e.onSuccess("restarted local java sip runtime");
        }).doOnSuccess(status -> log.info("SIP service restarted: " + status))
          .subscribe(restartService());
    }

    private Consumer<String> restartService() {
        return log::info;
    }
}
