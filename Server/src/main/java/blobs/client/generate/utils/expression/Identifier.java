package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.Assignable;
import blobs.client.generate.utils.statement.decleration.ImmutableDeclaration;
import blobs.client.generate.utils.statement.decleration.MutableDeclaration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;

public class Identifier implements Expression, Assignable, Declarable {
    protected final String name;

    private Identifier(String name) {
        this.name = sanitize(name);
    }

    private static String sanitize(String name) {
        String wordCharacters = name.replaceAll("\\W", Matcher.quoteReplacement("_"));
        if (wordCharacters.substring(0, 1).matches("\\d")) {
            wordCharacters = "_" + wordCharacters;
        }
        return wordCharacters;
    }

    public static Identifier of(String name) {
        return new Identifier(name);
    }

    public MutableDeclaration mutableDeclaration() {
        return MutableDeclaration.of(this);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier parameter = (Identifier) o;
        return Objects.equals(name, parameter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ')';
    }

    public InputStream inputStream(int indentation) {
        return new ByteArrayInputStream(name.getBytes());
    }
}
