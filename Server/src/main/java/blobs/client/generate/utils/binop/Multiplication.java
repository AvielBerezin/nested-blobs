package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class Multiplication extends BinaryOperationExpression {
    private Multiplication(Expression operand1, Expression operand2) {
        super("*", operand1, operand2);
    }

    public static Multiplication of(Expression operand1, Expression operand2) {
        return new Multiplication(operand1, operand2);
    }
}
