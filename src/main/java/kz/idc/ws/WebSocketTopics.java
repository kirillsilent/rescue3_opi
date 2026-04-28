package kz.idc.ws;

public enum WebSocketTopics {

    RESCUE("rescue"),
    WELCOME("welcome");

    public final String TOPIC;

    WebSocketTopics(String topic){
        TOPIC = topic;
    }
}
