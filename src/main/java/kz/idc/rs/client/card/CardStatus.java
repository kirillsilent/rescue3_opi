package kz.idc.rs.client.card;

public enum CardStatus {
    POST_INCIDENT("Post incident"),
    ALREADY_POSTED("Card already posted");

    public String STATUS;

    CardStatus(String status){
        STATUS = status;
    }
}
