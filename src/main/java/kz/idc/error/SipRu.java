package kz.idc.error;

public enum SipRu {
    CONNECTION("Локальный sip клиент не доступен"),
    READ_TIMEOUT("Ошибка чтения данных из sip клиента");

    public String EXCEPTION;

    SipRu(String exception){
        EXCEPTION = exception;
    }
}
