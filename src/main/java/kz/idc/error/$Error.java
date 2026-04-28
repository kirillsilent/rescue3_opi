package kz.idc.error;

import javax.inject.Singleton;

@Singleton
public class $Error {

    private static ErrorImpl error;

    public static ErrorImpl mk() {
        if (error == null) {
            error = new ErrorImpl();
        }
        return error;
    }

}
