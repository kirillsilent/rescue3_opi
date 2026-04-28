package kz.idc.utils.file;

public class $FileUtils {

    private $FileUtils(){}
    private static FileUtilsImpl mFileUtilsImpl;

    public static FileUtilsImpl mk() {
        if(mFileUtilsImpl==null){
            mFileUtilsImpl = new FileUtilsImpl();
        }
        return mFileUtilsImpl;
    }
}

