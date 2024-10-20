package blobs.client.utils;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class InputStreams {
    public static StringInputStream of(String data) {
        return StringInputStream.of(data);
    }

    public static InputStream of(Collection<InputStream> inputStreams) {
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }

    public static InputStream of(InputStream... inputStreams) {
        return new SequenceInputStream(Collections.enumeration(Arrays.asList(inputStreams)));
    }
}
