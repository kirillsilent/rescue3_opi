package kz.idc.rs.client.card;

import io.reactivex.Maybe;

public interface ApiCard {
    Maybe<Object> post();
    void unlockPostCard();
}
