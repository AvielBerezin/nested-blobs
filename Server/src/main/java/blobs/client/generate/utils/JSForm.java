package blobs.client.generate.utils;

import java.io.InputStream;

public interface JSForm {
    String indentationUnit = "  ";
    InputStream inputStream(int indentation);
}
