package blobs.world;

import blobs.client.sent.ClientBlob;
import blobs.client.sent.ClientView;
import blobs.utils.IterableMap;
import blobs.world.pivoted.PivotedBlobView;
import blobs.world.pivoted.PivotedBlobViewHome;
import blobs.world.pivoted.PivotedBlobViewHomeDispatcher;
import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Blob {
    private final List<Resident> residents;
    private Point2D position;
    private double r;

    protected Blob() {
        this(Cartesian.zero, 1);
    }

    protected Blob(Point2D position, double r) {
        this.residents = new LinkedList<>();
        this.position = position;
        this.r = r;
    }

    public abstract World world();

    public abstract Blob home();

    public Point2D position() {
        return position;
    }

    public double r() {
        return r;
    }

    public void position(Cartesian position) {
        this.position = position;
    }

    public void r(double r) {
        this.r = r;
    }

    public abstract void home(Blob home);

    public List<Resident> residents() {
        return residents;
    }

    public PivotedBlobView pivoted() {
        return new PivotedBlobViewPivot();
    }

    public int level() {
        int lvl = 0;
        Blob blob = this;
        while (blob != home()) {
            blob = home();
            lvl++;
        }
        return lvl;
    }

    protected String nestedToString() {
        return toString();
    }

    private class PivotedBlobViewPivot implements PivotedBlobView {
        @Override
        public Blob source() {
            return Blob.this;
        }

        @Override
        public Cartesian position() {
            return Cartesian.zero;
        }

        @Override
        public double r() {
            return 1;
        }

        @Override
        public PivotedBlobViewHome home() {
            if (Blob.this == Blob.this.home()) {
                return PivotedBlobViewHome.empty();
            }
            return PivotedBlobViewHome.home(Blob.this.home()
                                                     .pivoted()
                                                     .offset(Blob.this.position().negate().asCartesian())
                                                     .scale(1 / Blob.this.r()));
        }

        @Override
        public Iterable<PivotedBlobView> residents() {
            return IterableMap.of(Blob.this.residents(), resident ->
                    resident.pivoted()
                            .scale(resident.r())
                            .offset(resident.position().asCartesian()));
        }
    }
}
