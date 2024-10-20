package blobs.client.utils;

import java.io.ByteArrayInputStream;

public class StringInputStream extends ByteArrayInputStream {
    private StringInputStream(String data) {
        super(data.getBytes());
    }

    public static StringInputStream of(String data) {
        return new StringInputStream(data);
    }
}
