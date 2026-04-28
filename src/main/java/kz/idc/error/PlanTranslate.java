package kz.idc.error;

public enum PlanTranslate {
    NOT_FOUND("План отсутсвует");

    public String EXCEPTION;

    PlanTranslate(String exception){
        EXCEPTION = exception;
    }
}
