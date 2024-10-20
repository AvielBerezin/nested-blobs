package blobs.client.generate.utils.term;

import blobs.client.generate.utils.expression.Expression;
import blobs.client.utils.InputStreams;

import java.io.InputStream;

public class FuncTermReturn implements FuncTermStatement {
    private final Expression expression;

    private FuncTermReturn(Expression expression) {
        this.expression = expression;
    }

    public static FuncTermReturn of(Expression expression) {
        return new FuncTermReturn(expression);
    }

    @Override
    public InputStream inputStream(int indentation) {
        return InputStreams.of(InputStreams.of("return "),
                               expression.inputStream(indentation + 1),
                               InputStreams.of(";"));
    }
}
