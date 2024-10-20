package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.term.FuncOngoStatement;
import blobs.client.generate.utils.term.FuncTermBlock;
import blobs.client.generate.utils.term.IntoFuncOngoStatement;
import blobs.client.generate.utils.term.IntoFuncTermStatement;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Scope implements Expression {
    private final StackTraceElement[] creationTrace;
    private final List<FuncOngoStatement> ongoing;
    private FuncTermBlock block;

    protected Scope(StackTraceElement[] creationTrace) {
        this.creationTrace = creationTrace;
        this.ongoing = new LinkedList<>();
    }

    public static Scope create() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement[] refinedTrace = new StackTraceElement[trace.length - 2];
        System.arraycopy(trace, 2, refinedTrace, 0, refinedTrace.length);
        return new Scope(refinedTrace);
    }

    public Scope conclude(IntoFuncTermStatement termination) {
        if (this.block != null) {
            throw new IllegalArgumentException("scope was already concluded");
        }
        this.block = new FuncTermBlock(ongoing, termination.termination());
        return this;
    }

    public Scope addStatement(IntoFuncOngoStatement statement) {
        if (this.block != null) {
            throw new IllegalArgumentException("scope was already concluded");
        }
        ongoing.add(statement.statement());
        return this;
    }

    @Override
    public InputStream inputStream(int indentation) {
        if (this.block == null) {
            IllegalArgumentException exception = new IllegalArgumentException("function has yet to conclude");
            exception.setStackTrace(creationTrace);
            throw exception;
        }
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("((() => "));
        inputStreams.add(block.inputStream(indentation));
        inputStreams.add(InputStreams.of(")())"));
        return InputStreams.of(inputStreams);
    }
}
