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

    public Resident generateResident(Runnable onBeingEaten) {
        Blob blob = world;
        Cirle circle;
        mainLoop: while (true) {
            for (int i = 0; i < 10; i++) {
                Optional<Cirle> generatedCircle = tryGenerateCircle(blob);
                if (generatedCircle.isEmpty()) {
                    blob = blob.residents().get(random.nextInt(blob.residents().size()));
                } else {
                    circle = generatedCircle.get();
                    break mainLoop;
                }
            }
        }
        return new Resident(this, blob, circle.position(), circle.r(), onBeingEaten);
    }

    private Optional<Cirle> tryGenerateCircle(Blob blob) {
        double r;
        Point2D position;
        r = random.nextDouble(0.05, 0.2);
        position = Polar.randomInCircle(random).multiply(1 - r);
        Point2D finalPosition = position;
        double finalR = r;
        if (blob.residents().stream().noneMatch(resident -> {
            Cartesian displacement = resident.position().asCartesian().add(finalPosition.negate().asCartesian());
            double sr = resident.r() + finalR;
            return displacement.squared() < sr * sr;
        })) {
            return Optional.of(new Cirle(position, r));
        }
        return Optional.empty();
    }

    private record Cirle(Point2D position, double r) {
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
