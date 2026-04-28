package kz.idc.rs.services.client.api.requests;

public enum Auth {
    USERNAME("api"),
    PASSWORD("api");

    public String VAL;

    Auth(String value){
        VAL = value;
    }
}
