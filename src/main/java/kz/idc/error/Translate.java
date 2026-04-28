package kz.idc.error;

public enum Translate {
    TERMINAL_ALREADY_EXIST("Терминал с таким ip адресом уже зарегистрирован"),
    CONNECTION("Сервер недоступен, проверьте сетевое соеденение, или указанный адрес сервера"),
    NULL_POINTER_NETWORK_DEVICE("Сетевое устройство не настроено, вернитесь в сетевые настройки"),
    READ_TIMEOUT("Ошибка чтения данных"),
    NULL_POINTER_JSON("Данные с сервера не корректны, " +
            "возможно неверно указан адрес сервера или порт");

    public String EXCEPTION;

    Translate(String exception){
        EXCEPTION = exception;
    }
}
