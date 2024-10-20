package blobs.client.generate.utils.term;

import blobs.client.generate.utils.JSForm;
import blobs.client.generate.utils.expression.Expression;
import blobs.client.utils.InputStreams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FuncOngoPartialIf implements FuncOngoStatement {
    protected final Expression condition;
    private final List<FuncOngoStatement> ongoing;
    private FuncTermStatement termination;

    protected FuncOngoPartialIf(Expression condition) {
        this.condition = condition;
        ongoing = new LinkedList<>();
    }

    public FuncOngoPartialIf thenDo(FuncOngoStatement statement) {
        if (termination != null) {
            throw new RuntimeException("then clause was already concluded");
        }
        ongoing.add(statement);
        return this;
    }

    public FuncOngoStatement thenConclude(IntoFuncTermStatement termination) {
        if (this.termination != null) {
            throw new RuntimeException("then clause was already concluded");
        }
        this.termination = termination.termination();
        return this;
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(new ByteArrayInputStream("if (".getBytes()));
        inputStreams.add(condition.inputStream(indentation + 1));
        inputStreams.add(InputStreams.of("\n" + JSForm.indentationUnit.repeat(indentation + 1) + ") "));
        inputStreams.add((termination == null ? new FuncOngoBlock(ongoing)
                                              : new FuncTermBlock(ongoing, termination)
                         ).inputStream(indentation));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
