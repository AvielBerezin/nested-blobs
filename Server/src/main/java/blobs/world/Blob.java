package blobs.world;

import blobs.utils.IterableMap;

import java.util.LinkedList;
import java.util.List;

public abstract class Blob {
    private final List<Resident> residents;
    private Position position;
    private double r;

    protected Blob() {
        this(Position.zero, 1);
    }

    protected Blob(Position position, double r) {
        this.residents = new LinkedList<>();
        this.position = position;
        this.r = r;
    }

    public abstract World world();

    public abstract Blob home();

    public Position position() {
        return position;
    }

    public double r() {
        return r;
    }

    public void position(Position position) {
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
        return new PivotedBlobView() {
            @Override
            public Position position() {
                return Position.zero;
            }

            @Override
            public double r() {
                return 1;
            }

            @Override
            public PivotedBlobViewHome home() {
                if (Blob.this.home() == Blob.this.home().home()) {
                    return PivotedBlobViewHome.empty();
                }
                return PivotedBlobViewHome.home(Blob.this.home()
                                                         .pivoted()
                                                         .offset(position().negate())
                                                         .scale(1 / Blob.this.r()));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(Blob.this.residents(), resident ->
                        resident.pivoted()
                                .scale(resident.r())
                                .offset(resident.position()));
            }
        };
    }
}
