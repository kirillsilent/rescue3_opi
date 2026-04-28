package kz.idc.utils.storage;

public class $Storage {

    private $Storage(){}

    private static Storage mStorageImpl;

    public static Storage mk(){
        if(mStorageImpl == null){
            mStorageImpl = new StorageImpl();
        }
        return mStorageImpl;
    }
}
