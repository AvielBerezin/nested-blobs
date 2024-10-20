package blobs.client.generate.utils.binop;

import blobs.client.generate.utils.expression.Expression;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;

public abstract class BinaryOperationExpression implements Expression {
    protected final Expression operand1;
    protected final Expression operand2;
    private final String operator;

    public BinaryOperationExpression(String operator, Expression operand1, Expression operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(new ByteArrayInputStream("(".getBytes()));
        inputStreams.add(operand1.inputStream(indentation + 1));
        inputStreams.add(new ByteArrayInputStream(("\n" + indentationUnit.repeat(indentation)
                                                   + operator
                                                   + "\n" + indentationUnit.repeat(indentation + 1)
                                                  ).getBytes()));
        inputStreams.add(operand2.inputStream(indentation + 1));
        inputStreams.add(new ByteArrayInputStream(")".getBytes()));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
