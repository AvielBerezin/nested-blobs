package blobs.client.generate.utils.term;

import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class FuncOngoBlock implements FuncBlock {
    private final List<FuncOngoStatement> statements;

    public FuncOngoBlock(List<FuncOngoStatement> statements) {
        this.statements = statements;
    }

    @Override
    public InputStream inputStream(int indentation) {
        if (statements.isEmpty()) {
            return InputStreams.of(";");
        }
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("{"));
        for (FuncOngoStatement ongoingStatement : statements) {
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
            inputStreams.add(ongoingStatement.inputStream(indentation + 1));
        }
        inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation) + "}"));
        return InputStreams.of(inputStreams);
    }
}
