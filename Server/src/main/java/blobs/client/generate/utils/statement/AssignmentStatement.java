package blobs.client.generate.utils.statement;

import blobs.client.generate.utils.expression.Assignment;
import blobs.client.utils.InputStreams;

import java.io.InputStream;

public class AssignmentStatement implements Statement {
    private final Assignment assignment;

    private AssignmentStatement(Assignment assignment) {
        this.assignment = assignment;
    }

    public static AssignmentStatement of(Assignment assignment) {
        return new AssignmentStatement(assignment);
    }

    @Override
    public InputStream inputStream(int indentation) {
        return InputStreams.of(assignment.inputStream(indentation), InputStreams.of(";"));
    }
}
