package blobs.client.generate.utils.statement.decleration;

import blobs.client.generate.utils.expression.Expression;
import blobs.client.generate.utils.expression.Identifier;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ImmutableDeclaration implements Declaration {
    private final Identifier lhs;
    private final Expression rhs;

    private ImmutableDeclaration(Identifier lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static ImmutableDeclaration of(Identifier lhs, Expression rhs) {
        return new ImmutableDeclaration(lhs, rhs);
    }

    @Override
    public InputStream inputStream(int indentation) {
        List<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("const "));
        inputStreams.add(lhs.inputStream(indentation));
        inputStreams.add(InputStreams.of(" = "));
        inputStreams.add(rhs.inputStream(indentation + 1));
        inputStreams.add(InputStreams.of(";"));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
