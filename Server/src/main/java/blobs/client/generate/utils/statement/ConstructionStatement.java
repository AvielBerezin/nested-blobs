package blobs.client.generate.utils.statement;

import blobs.client.generate.utils.expression.Construction;
import blobs.client.utils.InputStreams;

import java.io.InputStream;

public class ConstructionStatement implements Statement {
    private final Construction construction;

    private ConstructionStatement(Construction construction) {
        this.construction = construction;
    }

    public static ConstructionStatement of(Construction construction) {
        return new ConstructionStatement(construction);
    }

    @Override
    public InputStream inputStream(int indentation) {
        return InputStreams.of(construction.inputStream(indentation), InputStreams.of(";"));
    }
}
