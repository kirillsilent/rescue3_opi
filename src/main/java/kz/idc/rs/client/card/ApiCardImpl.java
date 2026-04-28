package kz.idc.rs.client.card;

import io.micronaut.http.client.RxHttpClient;
import io.reactivex.Maybe;
import kz.idc.Application;
import kz.idc.dto.CardDTO;
import kz.idc.dto.RegCardDTO;
import kz.idc.error.$Error;
import kz.idc.rs.client.card.requests.$ApiCardRequests;
import kz.idc.rs.client.card.requests.ApiCardConfig;
import kz.idc.rs.client.card.requests.ApiCardRequests;
import kz.idc.utils.dates.$DateUtils;
import kz.idc.utils.storage.$Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ApiCardImpl implements ApiCard {

    private boolean isPosted = false;

    private final Logger log = LoggerFactory.getLogger(Application.class);
    private final RxHttpClient rxHttpClient;
    private final ApiCardRequests apiCardRequests;

    public ApiCardImpl(RxHttpClient rxHttpClient, ApiCardConfig apiCardConfig) {
        this.rxHttpClient = rxHttpClient;
        this.apiCardRequests = $ApiCardRequests.mk(apiCardConfig);
    }

    @Override
    public Maybe<Object> post() {
        if (!isPosted) {
            final Date date = new Date();
            log.info(CardStatus.POST_INCIDENT.STATUS + " : " + $DateUtils.mk().createDate(date));
            isPosted = true;
            return $Storage.mk().getIncident().toMaybe()
                    .flatMap(incident -> {
                        incident.setDate_time($DateUtils.mk().createDateForAPI(date));
                        return rxHttpClient.retrieve(apiCardRequests.incident(incident), RegCardDTO.class)
                                .firstElement()
                                .flatMap(regCardDTO -> result -> {
                                    log.warn("Desk ID: " + regCardDTO.getDeskid() + ", Card Number: " + regCardDTO.getCardnumber());
                                    result.onSuccess(regCardDTO);
                                })
                                .onErrorResumeNext(t -> result -> {
                                    isPosted = false;
                                    log.warn(t.getMessage());
                                    log.warn($Error.mk().translateCard(t.getMessage()));
                                    result.onSuccess($Error.mk().createErrorCardRu(t.getMessage()));
                                });
                    });
        } else {
            return Maybe.create(e -> {
                log.warn(CardStatus.ALREADY_POSTED.STATUS);
                e.onSuccess(CardDTO.create(CardStatus.ALREADY_POSTED.STATUS));
            });
        }
    }

    @Override
    public void unlockPostCard() {
        isPosted = false;
    }
}
