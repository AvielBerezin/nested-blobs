package blobs.client.generate.utils.expression.literal;

public class BooleanLiteral extends SimpleLiteral {
    private BooleanLiteral(Boolean bool) {
        super(bool);
    }

    public static BooleanLiteral of(Boolean number) {
        return new BooleanLiteral(number);
    }
}
