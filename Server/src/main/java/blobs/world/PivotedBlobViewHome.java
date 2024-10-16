package blobs.world;

public interface PivotedBlobViewHome {
    <Res> Res dispatch(PivotedBlobViewHomeDispatcher<Res, Res> dispatcher);

    static Home home(PivotedBlobView value) {
        return new Home(value);
    }

    static Empty empty() {
        return Empty.instance;
    }

    class Empty implements PivotedBlobViewHome {
        public static final Empty instance = new Empty();

        private Empty() {
        }

        @Override
        public <Res> Res dispatch(PivotedBlobViewHomeDispatcher<Res, Res> dispatcher) {
            return dispatcher.with(this);
        }
    }

    class Home implements PivotedBlobViewHome {
        private final PivotedBlobView value;

        private Home(PivotedBlobView value) {
            this.value = value;
        }

        @Override
        public <Res> Res dispatch(PivotedBlobViewHomeDispatcher<Res, Res> dispatcher) {
            return dispatcher.with(this);
        }

        public PivotedBlobView get() {
            return value;
        }
    }
}
