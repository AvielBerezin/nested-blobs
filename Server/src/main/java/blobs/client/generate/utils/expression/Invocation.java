package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.IntoStatement;
import blobs.client.generate.utils.statement.InvocationStatement;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Invocation implements Expression, IntoStatement {
    private final Expression function;
    private final List<Expression> arguments;

    private Invocation(Expression function) {
        this.function = function;
        arguments = new LinkedList<>();
    }

    public static Invocation of(Expression function) {
        return new Invocation(function);
    }

    public Invocation addArgument(Expression argument) {
        arguments.add(argument);
        return this;
    }

    @Override
    public InvocationStatement statement() {
        return InvocationStatement.of(this);
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(function.inputStream(indentation));
        inputStreams.add(InputStreams.of("("));
        for (Expression argument : arguments) {
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
            inputStreams.add(argument.inputStream(indentation + 1));
            inputStreams.add(InputStreams.of(","));
        }
        inputStreams.add(InputStreams.of((arguments.isEmpty() ? ""
                                                              : "\n" + indentationUnit.repeat(indentation))
                                         + ")"));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
