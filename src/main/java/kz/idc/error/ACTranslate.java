package kz.idc.error;

public enum ACTranslate {
    CONNECTION("Модуль по работе с аудио/камера устройствами не доступен"),
    READ_TIMEOUT("Ошибка чтения данных из аудио/камера модуля"),
    NULL_POINTER_AC_DEVICE("Устройство не настроено, вернитесь в настройки камеры и аудио");

    public String EXCEPTION;

    ACTranslate(String exception){
        EXCEPTION = exception;
    }
}
