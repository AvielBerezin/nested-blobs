package blobs.client.generate.utils.statement.decleration;

import blobs.client.generate.utils.expression.Identifier;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MutableDeclaration implements Declaration {
    private final Identifier identifier;

    private MutableDeclaration(Identifier identifier) {
        this.identifier = identifier;
    }

    public static MutableDeclaration of(Identifier identifier) {
        return new MutableDeclaration(identifier);
    }

    @Override
    public InputStream inputStream(int indentation) {
        List<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("let "));
        inputStreams.add(identifier.inputStream(indentation + 1));
        inputStreams.add(InputStreams.of(";"));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
