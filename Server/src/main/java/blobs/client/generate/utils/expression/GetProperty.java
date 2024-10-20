package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.Assignable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;

public class GetProperty implements Assignable, Expression {
    private final Expression object;
    private final Identifier property;

    private GetProperty(Expression object, Identifier property) {
        this.object = object;
        this.property = property;
    }

    public static GetProperty of(Expression object, Identifier property) {
        return new GetProperty(object, property);
    }

    @Override
    public InputStream inputStream(int indentation) {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(object.inputStream(indentation));
        inputStreams.add(new ByteArrayInputStream(".".getBytes()));
        inputStreams.add(property.inputStream(indentation + 1));
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }
}
