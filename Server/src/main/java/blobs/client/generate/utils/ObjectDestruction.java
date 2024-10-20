package blobs.client.generate.utils;

import blobs.client.generate.utils.expression.Declarable;
import blobs.client.generate.utils.expression.Identifier;
import blobs.client.utils.InputStreams;
import blobs.server.JSONSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ObjectDestruction implements Declarable {
    private final List<ObjectRecord> records;
    private final Set<String> keySet;
    private final StackTraceElement[] creationTrace;

    private ObjectDestruction(StackTraceElement[] creationTrace) {
        records = new LinkedList<>();
        keySet = new HashSet<>();
        this.creationTrace = creationTrace;
    }

    public static ObjectDestruction create() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement[] refinedTrace = new StackTraceElement[trace.length - 2];
        System.arraycopy(trace, 2, refinedTrace, 0, refinedTrace.length);
        return new ObjectDestruction(refinedTrace);
    }

    public ObjectDestruction addEntry(String key, ObjectDestruction destruction) {
        try {
            key = JSONSerializer.mapper.writeValueAsString(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (keySet.contains(key)) {
            throw new IllegalArgumentException("key " + key + " was already added");
        }
        keySet.add(key);
        records.add(new NestedForm(key, destruction));
        return this;
    }

    public ObjectDestruction addEntry(Identifier identifier) {
        if (keySet.contains(identifier.name())) {
            throw new IllegalArgumentException("key " + identifier.name() + " was already added");
        }
        keySet.add(identifier.name());
        records.add(new ImmediateForm(identifier));
        return this;
    }

    @Override
    public InputStream inputStream(int indentation) {
        if (records.isEmpty()) {
            RuntimeException emptyDestruction = new RuntimeException("empty destruction");
            emptyDestruction.setStackTrace(creationTrace);
            throw emptyDestruction;
        }
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        inputStreams.add(InputStreams.of("{"));
        for (ObjectRecord record : records) {
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1)));
            inputStreams.add(record.inputStream(indentation + 1));
        }
        inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation) + "}"));
        return InputStreams.of(inputStreams);
    }

    private interface ObjectRecord extends JSForm {
    }

    private record NestedForm(String key, ObjectDestruction destruction) implements ObjectRecord {
        @Override
        public InputStream inputStream(int indentation) {
            return InputStreams.of(InputStreams.of("\n" + indentationUnit.repeat(indentation)),
                                   InputStreams.of(key + ": "),
                                   destruction.inputStream(indentation + 1));
        }
    }

    private record ImmediateForm(Identifier identifier) implements ObjectRecord {
        @Override
        public InputStream inputStream(int indentation) {
            return identifier.inputStream(indentation);
        }
    }
}
