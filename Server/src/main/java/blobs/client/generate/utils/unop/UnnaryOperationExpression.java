package blobs.client.generate.utils.unop;

import blobs.client.generate.utils.expression.Expression;
import blobs.client.utils.InputStreams;

import java.io.InputStream;

public class UnnaryOperationExpression implements Expression {
    private final String operator;
    private final Expression operand;

    protected UnnaryOperationExpression(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public InputStream inputStream(int indentation) {
        return InputStreams.of(InputStreams.of("(" + operator),
                               operand.inputStream(indentation),
                               InputStreams.of(")"));
    }
}
