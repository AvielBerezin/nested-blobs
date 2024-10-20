package blobs.client.generate.utils.statement.decleration;

import blobs.client.generate.utils.statement.Statement;

import java.io.InputStream;

public interface Declaration extends Statement {
    InputStream inputStream(int indentation);
}
