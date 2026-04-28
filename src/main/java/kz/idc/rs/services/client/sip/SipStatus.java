package kz.idc.rs.services.client.sip;

public enum SipStatus {
    CALLING("Calling..."),
    ALREADY_CALLING("Sip client already calling"),
    CALL_END("Sip client call end"),
    SIP_ERROR("Sip client have error"),
    SIP_STARTED("Sip client service started"),
    SIP_RESTARTED("Sip client service restarted"),
    SIP_STOP("Sip client service stopped"),
    SIP_RESTART_ERROR("Can't restart sip client service"),
    SIP_STOP_ERROR("Can't stop sip client service"),
    SIP_FAILED("failed");


    public String STATUS;

    SipStatus(String status){
        STATUS = status;
    }
}
