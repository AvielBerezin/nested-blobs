package blobs.client.generate.utils.unop;

import blobs.client.generate.utils.expression.Expression;

public class Not extends UnnaryOperationExpression {
    private Not(Expression operand) {
        super("!", operand);
    }

    public static Not of(Expression operand) {
        return new Not(operand);
    }
}
