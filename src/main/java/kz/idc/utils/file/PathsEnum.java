package kz.idc.utils.file;

public enum PathsEnum {
    ROOT_PATH(".rescue"),
    MARQUEE("marquee"),
    AUDIO("audio"),
    PLAN("plan"),
    STORAGE ("storage"), 
    INCIDENT ("incident");

    public final String PATH;
    PathsEnum(String path){
        PATH = path;
    }
}
