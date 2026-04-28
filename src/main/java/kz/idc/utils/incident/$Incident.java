package kz.idc.utils.incident;

public class $Incident {

    private static Incident incident;
    private $Incident(){}

    public static Incident mk(){
        if(incident == null){
            incident = new IncidentImpl();
        }
        return incident;
    }
}
