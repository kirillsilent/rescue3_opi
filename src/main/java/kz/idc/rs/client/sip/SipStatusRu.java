package kz.idc.rs.client.sip;

public enum SipStatusRu {
    ALREADY_CALLING("Звонок активен");

    public String STATUS;

    SipStatusRu(String status){
        STATUS = status;
    }
}
