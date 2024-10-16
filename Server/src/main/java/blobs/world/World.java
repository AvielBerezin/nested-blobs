package blobs.world;

import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;
import blobs.world.point.Polar;

import java.util.*;

public class World extends Blob {
    private final List<Blob> all = new ArrayList<>(256);
    private final List<Resident> allResidents = new ArrayList<>(256);
    private final Random random;
    private final World world;
    private Blob home;

    public World(Random random) {
        this.world = this;
        this.home(this);
        all.add(this);
        this.random = random;
    }

    @Override
    public String toString() {
        return "World";
    }

    public Resident generateResident() {
        Blob blob = all.get(random.nextInt(all().size()));
        double r = 0.1;
        Point2D position;
        while (true) {
            position = Polar.randomInCircle(random).multiply(1 - r);
            Point2D finalPosition = position;
            if (blob.residents().stream().noneMatch(resident -> {
                Cartesian displacement = resident.position().asCartesian().add(finalPosition.negate().asCartesian());
                double sr = resident.r() + r;
                return displacement.squared() < sr * sr;
            })) {
                break;
            }
        }

        return new Resident(this, blob, position, r);
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public Blob home() {
        return home;
    }

    @Override
    public void home(Blob home) {
        this.home = home;
    }

    public List<Blob> all() {
        return all;
    }

    public List<Resident> allResidents() {
        return allResidents;
    }
}
