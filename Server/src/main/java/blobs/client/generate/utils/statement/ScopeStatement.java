package blobs.client.generate.utils.statement;

import blobs.client.generate.utils.IntoStatement;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScopeStatement implements Statement {
    private final List<Statement> statements;

    private ScopeStatement() {
        statements = new LinkedList<>();
    }

    public static ScopeStatement create() {
        return new ScopeStatement();
    }

    public ScopeStatement addStatement(IntoStatement element) {
        statements.add(element.statement());
        return this;
    }

    public InputStream inputStream(int indentation) {
        List<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("(() => {"));
        for (Statement statement : statements) {
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
            inputStreams.add(statement.inputStream(indentation + 1));
        }
        String spacing = statements.isEmpty() ? "" : "\n" + indentationUnit.repeat(indentation);
        inputStreams.add(InputStreams.of(spacing + "})();"));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
