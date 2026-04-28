package kz.idc.utils.incident;
import kz.idc.dto.IncidentDTO;

public class DefaultIncident {

    private DefaultIncident(){}

    private final static long Sid = 123;
    private final static String date_time = "";

    private final static String gts_phone = "7017387239";
    private final static String gts_fio = "Тревожная кнопка";
    private final static String gts_street = " ";
    private final static String gts_block = " ";
    private final static String gts_home = " ";
    private final static String gts_flat = " ";

    private final static String descr_event = "Срабатывание тревожной кнопки";
    private final static String street = "Тауелсиздик";
    private final static String cross_street = " ";
    private final static String numeric_home = "1";
    private final static String block = "Е";
    private final static String numeric_flat = " ";
    private final static String descr_place = " ";
    private final static int status_card = 0;

    private final static double xcoord = 7954202.05;
    private final static double ycoord = 6648233.71;

    private final static String user_id = "СУПЕР АДМИН";
    private final static int category_code = 24;
    private final static int region_id = 1;

    public static IncidentDTO incident() {

        IncidentDTO incidentDTO = new IncidentDTO();
        incidentDTO.setSid(Sid);
        incidentDTO.setDate_time(date_time);

        incidentDTO.setGts_phone(gts_phone);
        incidentDTO.setGts_fio(gts_fio);
        incidentDTO.setGts_street(gts_street);
        incidentDTO.setGts_block(gts_block);
        incidentDTO.setGts_home(gts_home);
        incidentDTO.setGts_flat(gts_flat);

        incidentDTO.setDescr_event(descr_event);
        incidentDTO.setStreet(street);
        incidentDTO.setCross_street(cross_street);
        incidentDTO.setNumeric_home(numeric_home);
        incidentDTO.setBlock(block);
        incidentDTO.setNumeric_flat(numeric_flat);
        incidentDTO.setDescr_place(descr_place);
        incidentDTO.setStatus_card(status_card);

        incidentDTO.setXcoord(xcoord);
        incidentDTO.setYcoord(ycoord);

        incidentDTO.setUser_id(user_id);
        incidentDTO.setCategory_code(category_code);
        incidentDTO.setRegion_id(region_id);

        return incidentDTO;
    }
}
