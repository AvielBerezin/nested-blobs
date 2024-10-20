package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class GreaterThen extends BinaryOperationExpression {
    private GreaterThen(Expression operand1, Expression operand2) {
        super(">", operand1, operand2);
    }

    public static GreaterThen of(Expression operand1, Expression operand2) {
        return new GreaterThen(operand1, operand2);
    }
}
