package blobs.world.pivoted;

import blobs.client.sent.ClientBlob;
import blobs.client.sent.ClientView;
import blobs.utils.IterableMap;
import blobs.world.Blob;
import blobs.world.point.Cartesian;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface PivotedBlobView {
    Blob source();
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

    default ClientView clientView(double windowRadius) {
        LinkedList<ClientBlob> result = new LinkedList<>();
        ArrayList<PivotedBlobView> safeRoute = getSafeRoute();
        for (int i = 0; i < safeRoute.size(); i++) {
            PivotedBlobView step = safeRoute.get(i);
            double sr = step.r() + windowRadius;
            if (step.position().squared() <= sr * sr) {
                result.add(step.clientBlob());
            }
            for (PivotedBlobView subStep : step.residents()) {
                if (i + 1 == getSafeRoute().size() ||
                    subStep.source() != safeRoute.get(i + 1).source()) {
                    result.addAll(subStep.clientBlobsInViewDownwards(windowRadius));
                }
            }
        }
        return new ClientView(windowRadius, result);
    }

    private ArrayList<PivotedBlobView> getSafeRoute() {
        LinkedList<PivotedBlobView> safeRoute = new LinkedList<>();
        AtomicReference<PivotedBlobView> blob = new AtomicReference<>(this);
        AtomicBoolean done = new AtomicBoolean(false);
        do {
            safeRoute.add(blob.get());
            blob.get()
                .home()
                .dispatch(PivotedBlobViewHomeDispatcher.init()
                                                       .<Runnable>withEmpty(emptyIgnored -> () -> done.set(true))
                                                       .withHome(homeIgnored -> () -> blob.set(homeIgnored.get())))
                .run();

        } while (!done.get());
        ArrayList<PivotedBlobView> result = new ArrayList<>(safeRoute.size());
        ListIterator<PivotedBlobView> iterator = safeRoute.listIterator(safeRoute.size());
        while (iterator.hasPrevious()) {
            result.add(iterator.previous());
        }
        return result;
    }

    private List<ClientBlob> clientBlobsInViewDownwards(double viewWindowRadius) {
        Cartesian position = position();
        double r = r();
        double sr = r + viewWindowRadius;
        if (position.squared() > sr * sr) {
            return Collections.emptyList();
        }
        List<ClientBlob> result = new LinkedList<>();
        result.add(clientBlob());
        for (PivotedBlobView resident : residents()) {
            result.addAll(resident.clientBlobsInViewDownwards(viewWindowRadius));
        }
        return result;
    }

    class PivotedBlobViewWrap implements PivotedBlobView {
        private final PivotedBlobView pivotedBlobView;

        public PivotedBlobViewWrap(PivotedBlobView delegate) {
            pivotedBlobView = delegate;
        }

        @Override
        public Blob source() {
            return pivotedBlobView.source();
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
