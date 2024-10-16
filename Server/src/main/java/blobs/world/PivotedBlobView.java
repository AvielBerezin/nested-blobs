package blobs.world;

import blobs.client.sent.ClientBlob;
import blobs.client.sent.ClientView;
import blobs.utils.IterableMap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface PivotedBlobView {
    double x();
    double y();
    double r();
    PivotedBlobViewHome home();
    Iterable<PivotedBlobView> residents();

    default PivotedBlobView offsetX(double offset) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public double x() {
                return super.x() + offset;
            }

            @Override
            public PivotedBlobViewHome home() {
                return super.home().dispatch(PivotedBlobViewHomeDispatcher.init()
                                                                          .<PivotedBlobViewHome>withEmpty(empty -> empty)
                                                                          .withHome(home -> PivotedBlobViewHome.home(home.get().offsetX(offset))));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.offsetX(offset));
            }
        };
    }

    default PivotedBlobView offsetY(double offset) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public double y() {
                return super.y() + offset;
            }

            @Override
            public PivotedBlobViewHome home() {
                return super.home().dispatch(PivotedBlobViewHomeDispatcher.init()
                                                                          .<PivotedBlobViewHome>withEmpty(empty -> empty)
                                                                          .withHome(home -> PivotedBlobViewHome.home(home.get().offsetY(offset))));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.offsetY(offset));
            }
        };
    }

    default PivotedBlobView scale(double factor) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public double x() {
                return super.x() * factor;
            }

            @Override
            public double y() {
                return super.y() * factor;
            }

            @Override
            public double r() {
                return super.r() * factor;
            }

            @Override
            public PivotedBlobViewHome home() {
                return super.home().dispatch(PivotedBlobViewHomeDispatcher.init()
                                                                          .<PivotedBlobViewHome>withEmpty(empty -> empty)
                                                                          .withHome(home -> PivotedBlobViewHome.home(home.get().scale(factor))));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.scale(factor));
            }
        };
    }

    default PivotedBlobView world() {
        return home().dispatch(PivotedBlobViewHomeDispatcher.init()
                                                            .withEmpty(empty -> this)
                                                            .withHome(home -> home.get().world()));
    }

    default ClientBlob clientBlob() {
        return new ClientBlob(x(), y(), r());
    }

    default ClientView clientView(double radius) {
        return new ClientView(radius, clientBlobsInView(radius));
    }

    default List<ClientBlob> clientBlobsInView(double viewWindowRadius) {
        double x = x();
        double y = y();
        double r = r();
        double sr = r + viewWindowRadius;
        if (x * x + y * y > sr * sr) {
            return Collections.emptyList();
        }
        List<ClientBlob> result = new LinkedList<>();
        result.add(clientBlob());
        for (PivotedBlobView resident : residents()) {
            result.addAll(resident.clientBlobsInView(viewWindowRadius));
        }
        return result;
    }

    class PivotedBlobViewWrap implements PivotedBlobView {
        private final PivotedBlobView pivotedBlobView;

        public PivotedBlobViewWrap(PivotedBlobView delegate) {
            pivotedBlobView = delegate;
        }

        @Override
        public double x() {
            return pivotedBlobView.x();
        }

        @Override
        public double y() {
            return pivotedBlobView.y();
        }

        @Override
        public double r() {
            return pivotedBlobView.r();
        }

        @Override
        public PivotedBlobViewHome home() {
            return pivotedBlobView.home();
        }

        @Override
        public Iterable<PivotedBlobView> residents() {
            return pivotedBlobView.residents();
        }
    }
}
