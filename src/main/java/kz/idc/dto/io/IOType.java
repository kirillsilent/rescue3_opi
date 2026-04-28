package kz.idc.dto.io;

public enum IOType {
    NETWORK("network"),
    AUDIO_INPUT("input"),
    AUDIO_OUTPUT("output"),
    CAMERA("camera");

    public final String DEVICE;

    IOType(String device) {
        DEVICE = device;
    }
}

