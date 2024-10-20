package blobs.client.generate.utils.expression.literal;

import blobs.client.generate.utils.expression.Expression;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static blobs.server.JSONSerializer.mapper;

public abstract class SimpleLiteral implements Expression {
    protected final byte[] data;

    protected SimpleLiteral(Object obj) {
        try {
            data = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream inputStream(int indentation) {
        return new ByteArrayInputStream(data);
    }
}
