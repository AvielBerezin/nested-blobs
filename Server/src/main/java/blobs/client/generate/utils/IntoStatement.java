package blobs.client.generate.utils;

import blobs.client.generate.utils.statement.Statement;
import blobs.client.generate.utils.term.IntoFuncOngoStatement;

public interface IntoStatement extends JSForm, IntoFuncOngoStatement {
    @Override
    Statement statement();
}
