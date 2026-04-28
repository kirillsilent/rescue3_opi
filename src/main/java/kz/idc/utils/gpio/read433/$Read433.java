package kz.idc.utils.gpio.read433;

public class $Read433 {
    private static Read433Impl read433;

    public static Read433Impl mk(){
        if(read433 == null){
            read433 = new Read433Impl();
        }
        return read433;
    }
}
