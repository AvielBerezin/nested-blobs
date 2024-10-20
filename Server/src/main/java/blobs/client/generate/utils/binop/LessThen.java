package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

public class LessThen extends BinaryOperationExpression {
    private LessThen(Expression operand1, Expression operand2) {
        super("<", operand1, operand2);
    }

    public static LessThen of(Expression operand1, Expression operand2) {
        return new LessThen(operand1, operand2);
    }
}
