package kz.idc.utils.file;

public enum FileExtensions {
    JSON(".json"),
    WAV(".wav");

    public final String EXTENSION;

    FileExtensions(String extension){
        EXTENSION = extension;
    }
}
