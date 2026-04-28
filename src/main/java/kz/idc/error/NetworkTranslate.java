package kz.idc.error;

public enum NetworkTranslate {
    CONNECTION("Модуль по работе с сетью не доступен"),
    READ_TIMEOUT("Ошибка чтения данных из сетевого модуля"),
    NULL_POINTER_NETWORK_DEVICE("Сетевое устройство не настроено, вернитесь в сетевые настройки");

    public String EXCEPTION;

    NetworkTranslate(String exception){
        EXCEPTION = exception;
    }
}
