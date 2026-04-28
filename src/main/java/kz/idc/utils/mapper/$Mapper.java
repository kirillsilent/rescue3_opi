package kz.idc.utils.mapper;

public class $Mapper {

    private static Mapper mapper;

    public static Mapper mk(){
        if(mapper == null){
            mapper = new MapperImpl();
        }
        return mapper;
    }
}

