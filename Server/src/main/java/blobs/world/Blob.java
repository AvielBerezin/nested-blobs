package blobs.world;

import blobs.utils.IterableMap;

import java.util.List;

public interface Blob {
    World world();

    Blob home();

    double x();

    double y();

    double r();

    void home(Blob home);

    void x(double x);

    void y(double y);

    void r(double r);

    List<Resident> residents();

    default PivotedBlobView pivoted() {
        return new PivotedBlobView() {
            @Override
            public double x() {
                return 0;
            }

            @Override
            public double y() {
                return 0;
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
                                                         .offsetX(-Blob.this.x())
                                                         .offsetY(-Blob.this.y())
                                                         .scale(1 / Blob.this.r()));
            }

            @Override
            public Iterable<PivotedBlobView> residents() {
                return IterableMap.of(Blob.this.residents(), resident ->
                        resident.pivoted()
                                .scale(resident.r())
                                .offsetX(resident.x())
                                .offsetY(resident.y()));
            }
        };
    }
}
