package kz.idc.rs.services.client.ac;


public class $ACTools {

    private $ACTools(){}

    private static ACTools mACToolsImpl;

    public static ACTools mk() {
        if (mACToolsImpl == null) {
            mACToolsImpl = new ACToolsImpl();
        }
        return mACToolsImpl;
    }
}
