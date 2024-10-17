package blobs.world;

import blobs.utils.IterableMap;
import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Blob {
    private final boolean isHuman;
    private final List<Resident> residents;
    private Point2D position;
    private double r;

    protected Blob() {
        this(Cartesian.zero, 1, false);
    }

    protected Blob(Point2D position, double r, boolean isHuman) {
        this.residents = new LinkedList<>();
        this.position = position;
        this.r = r;
        this.isHuman = isHuman;
    }

    public abstract <Res> Res dispatch(Function<World, Res> onWorld,
                                       Function<Resident, Res> onResident);

    public abstract Optional<Blob> home();

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

    public List<Resident> residents() {
        return residents;
    }

    public PivotedBlobView pivoted() {
        return new PivotedBlobViewPivot();
    }

    public abstract int level();

    protected String nestedToString() {
        return toString();
    }

    private class PivotedBlobViewPivot extends PivotedBlobView {
        private PivotedBlobViewPivot() {
            super(isHuman);
        }

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
        public Optional<PivotedBlobView> home() {
            return Blob.this.home().map(blob -> blob.pivoted()
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
