package kz.idc.utils.xxh;

import net.jpountz.xxhash.StreamingXXHash64;

import java.io.InputStream;

public interface GenerateXHH {
    StreamingXXHash64 init();
    StreamingXXHash64 addByteToXXH64(byte[] bytes, StreamingXXHash64 xxHash64);
    String getChecksumFromStream(InputStream inputStream);
    String getXXH64Checksum(StreamingXXHash64 xxHash64);
}
