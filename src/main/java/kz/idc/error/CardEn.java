package kz.idc.error;

public enum CardEn {
    CONNECTION("Card integration server unavailable"),
    READ_TIMEOUT("Timeout error integration server");

    public String EXCEPTION;

    CardEn(String exception){
        EXCEPTION = exception;
    }
}
