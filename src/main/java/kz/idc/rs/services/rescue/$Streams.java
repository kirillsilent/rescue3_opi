package kz.idc.rs.services.rescue;


public class $Streams {

    private $Streams(){}
    private static Streams streams;

    public static Streams mk(){
        if(streams == null){
            streams = new StreamsImpl();
        }
        return streams;
    }
}
