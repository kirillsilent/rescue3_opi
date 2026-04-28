package kz.idc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

@Introspected
@Getter
@Setter
public class IncidentDTO {
    @JsonProperty("Sid")
    private Long Sid;
    private String date_time;
    private String gts_phone;
    private String gts_fio;
    @JsonProperty("Gts_street")
    private String gts_street;
    private String gts_block;
    private String gts_home;
    private String gts_flat;

    private String descr_event;
    private String street;
    private String cross_street;
    private String numeric_home;
    private String block;
    private String numeric_flat;
    private String descr_place;
    private int status_card;

    private double xcoord;
    private double ycoord;

    private String user_id;
    private int category_code;
    private int region_id;

}
