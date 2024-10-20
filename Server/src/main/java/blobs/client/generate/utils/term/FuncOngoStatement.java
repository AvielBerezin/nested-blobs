package blobs.client.generate.utils.term;

public interface FuncOngoStatement extends FuncStatement, IntoFuncOngoStatement {
    @Override
    default FuncOngoStatement statement() {
        return this;
    }
}
