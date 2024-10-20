package blobs.client.generate.utils.term;

import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class FuncTermBlock implements FuncBlock {
    private final List<FuncOngoStatement> ongoing;
    private final FuncTermStatement termination;

    public FuncTermBlock(List<FuncOngoStatement> ongoing,
                         FuncTermStatement termination) {
        this.ongoing = ongoing;
        this.termination = termination;
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("{"));
        for (FuncOngoStatement ongoingStatement : ongoing) {
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
            inputStreams.add(ongoingStatement.inputStream(indentation + 1));
        }
        inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
        inputStreams.add(termination.inputStream(indentation + 1));
        inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation) + "}"));
        return InputStreams.of(inputStreams);
    }
}
