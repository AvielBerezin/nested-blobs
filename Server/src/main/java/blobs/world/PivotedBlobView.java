package blobs.world;

import blobs.client.sent.ClientBlob;
import blobs.client.sent.ClientView;
import blobs.utils.IterableMap;
import blobs.world.point.Cartesian;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PivotedBlobView {

    private final boolean isHuman;

    public PivotedBlobView(boolean isHuman) {
        this.isHuman = isHuman;
    }

    public abstract Blob source();
    public abstract Cartesian position();
    public abstract double r();
    public abstract Optional<PivotedBlobView> home();
    public abstract Iterable<PivotedBlobView> residents();

    public PivotedBlobView offset(Cartesian offset) {
        return new PivotedBlobViewWrap(this) {
            @Override
            public Cartesian position() {
                return super.position().add(offset);
            }

            @Override
            public Optional<PivotedBlobView> home() {
                return super.home().map(pivotedBlobView -> pivotedBlobView.offset(offset));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.offset(offset));
            }
        };
    }

    public PivotedBlobView scale(double factor) {
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
            public Optional<PivotedBlobView> home() {
                return super.home().map(pivotedBlobView -> pivotedBlobView.scale(factor));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(super.residents(), resident -> resident.scale(factor));
            }
        };
    }

    public PivotedBlobView world() {
        return home().map(PivotedBlobView::world).orElse(this);
    }

    private ClientBlob clientBlob() {
        return new ClientBlob(position().x(), position().y(), r(), isHuman);
    }

    public ClientView clientView(double windowRadius) {
        LinkedList<ClientBlob> result = new LinkedList<>();
        ArrayList<PivotedBlobView> safeRoute = getSafeRoute();
        for (int i = 0; i < safeRoute.size() - 1; i++) {
            PivotedBlobView step = safeRoute.get(i);
            double sr = step.r() + windowRadius;
            if (step.position().squared() <= sr * sr) {
                result.add(step.clientBlob());
            }
            for (PivotedBlobView subStep : step.residents()) {
                if (subStep.source() != safeRoute.get(i + 1).source()) {
                    result.addAll(subStep.clientBlobsInViewDownwards(windowRadius));
                }
            }
        }
        List<ClientBlob> blobsFromPivot = safeRoute.get(safeRoute.size() - 1).clientBlobsInViewDownwards(windowRadius);
        result.addAll(blobsFromPivot.subList(1, blobsFromPivot.size()));
        return new ClientView(windowRadius, blobsFromPivot.get(0), result);
    }

    public ArrayList<PivotedBlobView> getSafeRoute() {
        LinkedList<PivotedBlobView> safeRoute = new LinkedList<>();
        AtomicReference<PivotedBlobView> blob = new AtomicReference<>(this);
        AtomicBoolean done = new AtomicBoolean(false);
        do {
            safeRoute.add(blob.get());
            blob.get().home().ifPresentOrElse(blob::set, () -> done.set(true));

        } while (!done.get());
        ArrayList<PivotedBlobView> result = new ArrayList<>(safeRoute.size());
        ListIterator<PivotedBlobView> iterator = safeRoute.listIterator(safeRoute.size());
        while (iterator.hasPrevious()) {
            result.add(iterator.previous());
        }
        return result;
    }

    public List<ClientBlob> clientBlobsInViewDownwards(double viewWindowRadius) {
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

    public static class PivotedBlobViewWrap extends PivotedBlobView {
        private final PivotedBlobView pivotedBlobView;

        public PivotedBlobViewWrap(PivotedBlobView delegate) {
            super(delegate.isHuman);
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
        public Optional<PivotedBlobView> home() {
            return pivotedBlobView.home();
        }

        @Override
        public Iterable<PivotedBlobView> residents() {
            return pivotedBlobView.residents();
        }
    }
}
