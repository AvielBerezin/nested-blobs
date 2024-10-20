package blobs.client.generate.utils.statement;

import blobs.client.generate.utils.IntoStatement;
import blobs.client.generate.utils.term.FuncOngoStatement;

import java.io.InputStream;

public interface Statement extends IntoStatement, FuncOngoStatement {
    InputStream inputStream(int indentation);

    @Override
    default Statement statement() {
        return this;
    }
}
