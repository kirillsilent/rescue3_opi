package kz.idc.rs.client.card.requests;

public final class $ApiCardRequests {

    private $ApiCardRequests(){}

    private static ApiCardRequests apiCardRequests;

    public static ApiCardRequests mk(ApiCardConfig apiCardConfig){
        if(apiCardRequests == null){
            apiCardRequests = new ApiCardRequestsImpl(apiCardConfig);
        }
        return apiCardRequests;
    }

}
