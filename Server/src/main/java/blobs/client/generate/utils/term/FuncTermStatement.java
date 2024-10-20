package blobs.client.generate.utils.term;

public interface FuncTermStatement extends FuncStatement, IntoFuncTermStatement {
    @Override
    default FuncTermStatement termination() {
        return this;
    }
}
