package blobs.client.generate.utils.unop;

import blobs.client.generate.utils.expression.Expression;

public class IntoNum extends UnnaryOperationExpression {
    private IntoNum(Expression operand) {
        super("+", operand);
    }

    public static IntoNum of(Expression operand) {
        return new IntoNum(operand);
    }
}
