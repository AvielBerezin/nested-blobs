package blobs.client.generate.utils.expression.literal;

public class NumberLiteral extends SimpleLiteral {
    private NumberLiteral(Number number) {
        super(number);
    }

    public static NumberLiteral of(Number number) {
        return new NumberLiteral(number);
    }
}
