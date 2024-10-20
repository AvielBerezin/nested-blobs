package blobs.client.generate.utils.statement;

import blobs.client.generate.utils.expression.Invocation;
import blobs.client.utils.InputStreams;

import java.io.InputStream;

public class InvocationStatement implements Statement {
    private final Invocation invocation;

    private InvocationStatement(Invocation invocation) {
        this.invocation = invocation;
    }

    public static InvocationStatement of(Invocation invocation) {
        return new InvocationStatement(invocation);
    }

    @Override
    public InputStream inputStream(int indentation) {
        return InputStreams.of(invocation.inputStream(indentation), InputStreams.of(";"));
    }
}
