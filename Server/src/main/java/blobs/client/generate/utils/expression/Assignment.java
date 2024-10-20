package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.Assignable;
import blobs.client.generate.utils.IntoStatement;
import blobs.client.generate.utils.statement.AssignmentStatement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Assignment implements Expression, IntoStatement {
    private final Assignable lhs;
    private final Expression rhs;

    private Assignment(Assignable lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static Assignment of(Assignable lhs, Expression rhs) {
        return new Assignment(lhs, rhs);
    }

    @Override
    public AssignmentStatement statement() {
        return AssignmentStatement.of(this);
    }

    @Override
    public InputStream inputStream(int indentation) {
        List<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(lhs.inputStream(indentation));
        inputStreams.add(new ByteArrayInputStream(" = ".getBytes()));
        inputStreams.add(rhs.inputStream(indentation));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
