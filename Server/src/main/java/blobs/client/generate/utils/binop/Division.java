package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class Division extends BinaryOperationExpression {
    private Division(Expression operand1, Expression operand2) {
        super("/", operand1, operand2);
    }

    public static Division of(Expression operand1, Expression operand2) {
        return new Division(operand1, operand2);
    }
}
