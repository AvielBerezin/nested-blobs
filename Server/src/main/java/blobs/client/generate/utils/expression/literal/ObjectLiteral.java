package blobs.client.generate.utils.expression.literal;

import blobs.client.generate.utils.expression.Expression;

public interface ObjectLiteral extends Expression {
    static ObjectSimpleLiteral of(Record obj) {
        return new ObjectSimpleLiteral(obj);
    }
    static GeneralObjectLiteral create() {
        return new GeneralObjectLiteral();
    }
}
