package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class Subtraction extends BinaryOperationExpression {
    private Subtraction(Expression operand1, Expression operand2) {
        super("-", operand1, operand2);
    }

    public static Subtraction of(Expression operand1, Expression operand2) {
        return new Subtraction(operand1, operand2);
    }
}
