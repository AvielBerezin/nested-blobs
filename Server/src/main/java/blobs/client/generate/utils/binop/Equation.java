package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class Equation extends BinaryOperationExpression {
    private Equation(Expression operand1, Expression operand2) {
        super("===", operand1, operand2);
    }

    public static Equation of(Expression operand1, Expression operand2) {
        return new Equation(operand1, operand2);
    }
}
