package blobs.client.generate.utils.expression.literal;

import blobs.client.generate.utils.JSForm;
import blobs.client.generate.utils.expression.Expression;
import blobs.client.generate.utils.expression.Identifier;
import blobs.client.utils.InputStreams;
import blobs.server.JSONSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GeneralObjectLiteral implements ObjectLiteral {
    private final List<ObjectRecord> records;
    private final Set<String> keySet;

    protected GeneralObjectLiteral() {
        records = new LinkedList<>();
        keySet = new HashSet<>();
    }

    public GeneralObjectLiteral addEntry(String key, Expression expression) {
        try {
            key = JSONSerializer.mapper.writeValueAsString(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (keySet.contains(key)) {
            throw new IllegalArgumentException("key " + key + " was already added");
        }
        keySet.add(key);
        records.add(new FullRecordForm(key, expression));
        return this;
    }

    public GeneralObjectLiteral addEntry(Identifier identifier) {
        if (keySet.contains(identifier.name())) {
            throw new IllegalArgumentException("key " + identifier.name() + " was already added");
        }
        keySet.add(identifier.name());
        records.add(new ShortRecordForm(identifier));
        return this;
    }

    @Override
    public InputStream inputStream(int indentation) {
        if (records.isEmpty()) {
            return InputStreams.of("{}");
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

    private record FullRecordForm(String key, Expression expression) implements ObjectRecord {
        @Override
        public InputStream inputStream(int indentation) {
            return InputStreams.of(InputStreams.of("\n" + indentationUnit.repeat(indentation)),
                                   InputStreams.of(key + ": "),
                                   expression.inputStream(indentation + 1));
        }
    }

    private record ShortRecordForm(Identifier identifier) implements ObjectRecord {
        @Override
        public InputStream inputStream(int indentation) {
            return identifier.inputStream(indentation);
        }
    }
}
