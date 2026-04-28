package kz.idc.rs.services.client.api.requests;

public class $APIRequests {
    private static APIRequestsImpl apiRequests;

    public static APIRequests mk(APIConfiguration apiConfiguration){
        if(apiRequests == null){
            apiRequests = new APIRequestsImpl(apiConfiguration);
        }
        return apiRequests;
    }
}
