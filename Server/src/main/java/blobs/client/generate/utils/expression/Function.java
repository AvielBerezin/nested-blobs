package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.JSForm;
import blobs.client.generate.utils.term.FuncOngoStatement;
import blobs.client.generate.utils.term.FuncTermBlock;
import blobs.client.generate.utils.term.IntoFuncOngoStatement;
import blobs.client.generate.utils.term.IntoFuncTermStatement;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Function implements Expression {
    private final StackTraceElement[] creationTrace;
    private final List<Declarable> parameters;
    private final Set<Declarable> parametersSet;
    private final LinkedList<FuncOngoStatement> ongoing;
    private FuncTermBlock body;

    protected Function(StackTraceElement[] creationTrace) {
        this.creationTrace = creationTrace;
        parameters = new LinkedList<>();
        parametersSet = new HashSet<>();
        this.ongoing = new LinkedList<>();
    }

    public static Function create() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement[] refinedTrace = new StackTraceElement[trace.length - 2];
        System.arraycopy(trace, 2, refinedTrace, 0, refinedTrace.length);
        return new Function(refinedTrace);
    }

    public Function addParameter(Declarable parameter) {
        if (parametersSet.contains(parameter)) {
            throw new IllegalArgumentException("method already contains " + parameter);
        }
        parameters.add(parameter);
        parametersSet.add(parameter);
        return this;
    }

    public Function conclude(IntoFuncTermStatement termination) {
        if (this.body != null) {
            throw new IllegalArgumentException("function was already concluded");
        }
        this.body = new FuncTermBlock(ongoing, termination.termination());
        return this;
    }

    public Function addStatement(IntoFuncOngoStatement statement) {
        if (this.body != null) {
            throw new IllegalArgumentException("function was already concluded");
        }
        ongoing.add(statement.statement());
        return this;
    }

    @Override
    public InputStream inputStream(int indentation) {
        if (this.body == null) {
            IllegalArgumentException exception = new IllegalArgumentException("function has yet to conclude");
            exception.setStackTrace(creationTrace);
            throw exception;
        }
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("((("));
        for (Declarable parameter : parameters) {
            inputStreams.add(InputStreams.of("\n" + JSForm.indentationUnit.repeat(indentation + 1)));
            inputStreams.add(parameter.inputStream(indentation + 1));
        }
        inputStreams.add(InputStreams.of(") => "));
        inputStreams.add(body.inputStream(indentation));
        inputStreams.add(InputStreams.of("))"));
        return InputStreams.of(inputStreams);
    }
}
