package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class Addition extends BinaryOperationExpression {
    private Addition(Expression operand1, Expression operand2) {
        super("+", operand1, operand2);
    }

    public static Addition of(Expression operand1, Expression operand2) {
        return new Addition(operand1, operand2);
    }
}
