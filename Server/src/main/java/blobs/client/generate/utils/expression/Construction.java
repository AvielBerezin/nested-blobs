package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.IntoStatement;
import blobs.client.generate.utils.statement.ConstructionStatement;
import blobs.client.utils.InputStreams;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Construction implements Expression, IntoStatement {
    private final Expression constructor;
    private final List<Expression> arguments;

    protected Construction(Expression constructor) {
        this.constructor = constructor;
        this.arguments = new LinkedList<>();
    }

    public static Construction of(Expression function) {
        return new Construction(function);
    }

    public Construction addArgument(Expression argument) {
        arguments.add(argument);
        return this;
    }

    @Override
    public ConstructionStatement statement() {
        return ConstructionStatement.of(this);
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("new ("));
        inputStreams.add(constructor.inputStream(indentation));
        inputStreams.add(InputStreams.of(") ("));
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
