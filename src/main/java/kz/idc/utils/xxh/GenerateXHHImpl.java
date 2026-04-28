package kz.idc.utils.xxh;

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GenerateXHHImpl implements GenerateXHH {

    @Override
    public StreamingXXHash64 init() {
        XXHashFactory factory = XXHashFactory.fastestInstance();
        int seed = 0;
        return factory.newStreamingHash64(seed);
    }

    @Override
    public StreamingXXHash64 addByteToXXH64(byte[] bytes, StreamingXXHash64 xxHash64) {
        xxHash64.asChecksum().update(bytes);
        return xxHash64;
    }

    @Override
    public String getChecksumFromStream(InputStream inputStream) {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        StreamingXXHash64 hash64 = init();
        byte[] buf = new byte[8192];
        try {
            for (;;) {
                int read = bis.read(buf);
                if (read == -1) {
                    break;
                }
                hash64.update(buf, 0, read);
            }
            bis.close();
            return Long.toHexString(hash64.asChecksum().getValue());
        }catch (IOException e){
            return "";
        }
    }

    @Override
    public String getXXH64Checksum(StreamingXXHash64 xxHash64) {
        return Long.toHexString(xxHash64.asChecksum().getValue());
    }
}
