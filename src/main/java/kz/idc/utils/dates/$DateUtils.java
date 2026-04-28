package kz.idc.utils.dates;

public class $DateUtils {

    private static DateUtils dateUtils;

    public static DateUtils mk(){
        if(dateUtils == null){
            dateUtils = new DateUtilsImpl();
        }
        return dateUtils;
    }

}
