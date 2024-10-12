package blobs.world;

import java.util.List;

public interface Blob {
    World world();

    List<Resident> residents();

    Blob home();

    void home(Blob home);

    double x();

    void x(double x);

    double y();

    void y(double y);

    double r();

    void r(double r);
}
