package kz.idc.utils.xxh;

public class $GenerateXXH {
    private static GenerateXHHImpl generateXXH;

    public static GenerateXHHImpl mk(){
        if(generateXXH == null){
            generateXXH = new GenerateXHHImpl();
        }
        return generateXXH;
    }
}
