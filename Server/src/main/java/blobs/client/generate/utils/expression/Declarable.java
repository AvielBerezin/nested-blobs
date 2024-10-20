package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.JSForm;
import blobs.client.generate.utils.statement.decleration.ImmutableDeclaration;

public interface Declarable extends JSForm {
    default ImmutableDeclaration declareAs(Expression rhs) {
        return ImmutableDeclaration.of(this, rhs);
    }
}
