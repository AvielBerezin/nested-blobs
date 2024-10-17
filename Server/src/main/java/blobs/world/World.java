package blobs.world;

import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;
import blobs.world.point.Polar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class World extends Blob {
    private final List<Blob> all = new ArrayList<>(256);
    private final List<Resident> allResidents = new ArrayList<>(256);
    private final Random random;
    private final World world;

    public World(Random random) {
        this.world = this;
        all.add(this);
        this.random = random;
    }

    @Override
    public String toString() {
        return "World";
    }

    public Resident generateResident() {
        Blob blob;
        Point2D position;
        double r;
        while (true) {
            blob = all.get(random.nextInt(all().size()));
            r = random.nextDouble(0.1, 0.3);
            position = Polar.randomInCircle(random).multiply(1 - r);
            Point2D finalPosition = position;
            double finalR = r;
            if (blob.residents().stream().noneMatch(resident -> {
                Cartesian displacement = resident.position().asCartesian().add(finalPosition.negate().asCartesian());
                double sr = resident.r() + finalR;
                return displacement.squared() < sr * sr;
            })) {
                break;
            }
        }
        return new Resident(this, blob, position, r);
    }

    @Override
    public <Res> Res dispatch(Function<World, Res> onWorld, Function<Resident, Res> onResident) {
        return onWorld.apply(this);
    }

    @Override
    public Optional<Blob> home() {
        return Optional.empty();
    }

    @Override
    public int level() {
        return 0;
    }

    public List<Blob> all() {
        return all;
    }

    public List<Resident> allResidents() {
        return allResidents;
    }
}
