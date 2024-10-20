package blobs.client.generate.utils;

import blobs.client.generate.utils.expression.Assignment;
import blobs.client.generate.utils.expression.Expression;

import java.io.InputStream;

public interface Assignable extends JSForm {
    InputStream inputStream(int indentation);
    default Assignment assign(Expression expression) {
        return Assignment.of(this, expression);
    }
}
