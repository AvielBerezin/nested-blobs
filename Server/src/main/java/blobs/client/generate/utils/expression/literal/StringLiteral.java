package blobs.client.generate.utils.expression.literal;

public class StringLiteral extends SimpleLiteral {
    private StringLiteral(String string) {
        super(string);
    }

    public static StringLiteral of(String literal) {
        return new StringLiteral(literal);
    }
}
