package kz.idc.error;

public enum CardRu {
    CONNECTION("Сервер интеграции в ДВД не доступен"),
    READ_TIMEOUT("Ошибка чтения данных с сервера интеграции ДВД");

    public String EXCEPTION;

    CardRu(String exception){
        EXCEPTION = exception;
    }
}
