package blobs.world.pivoted;

import blobs.client.sent.ClientBlob;
import blobs.client.sent.ClientView;
import blobs.utils.IterableMap;
import blobs.world.point.Cartesian;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface PivotedBlobView {
    Cartesian position();
    double r();
    PivotedBlobViewHome home();
    Iterable<PivotedBlobView> residents();

    default PivotedBlobView offset(Cartesian offset) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public Cartesian position() {
                return super.position().add(offset);
            }

            @Override
            public PivotedBlobViewHome home() {
                return super.home().dispatch(PivotedBlobViewHomeDispatcher.init()
                                                                          .<PivotedBlobViewHome>withEmpty(empty -> empty)
                                                                          .withHome(home -> PivotedBlobViewHome.home(home.get().offset(offset))));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.offset(offset));
            }
        };
    }

    default PivotedBlobView scale(double factor) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public Cartesian position() {
                return super.position().multiply(factor);
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
        return new ClientBlob(position().x(), position().y(), r());
    }

    default ClientView clientView(double radius) {
        return new ClientView(radius, clientBlobsInView(radius));
    }

    default List<ClientBlob> clientBlobsInView(double viewWindowRadius) {
        Cartesian position = position();
        double r = r();
        double sr = r + viewWindowRadius;
        if (position.squared() > sr * sr) {
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
        public Cartesian position() {
            return pivotedBlobView.position();
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
