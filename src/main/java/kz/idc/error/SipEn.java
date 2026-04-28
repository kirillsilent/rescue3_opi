package kz.idc.error;

public enum SipEn {
    CONNECTION("Local sip client unavailable"),
    READ_TIMEOUT("Timeout error sip client");

    public String EXCEPTION;

    SipEn(String exception){
        EXCEPTION = exception;
    }
}
