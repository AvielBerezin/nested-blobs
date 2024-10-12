package blobs.world;

import java.util.*;

public class World implements Blob {
    private final List<Blob> all = new ArrayList<>(256);
    private final List<Resident> allResidents = new ArrayList<>(256);
    private final Random random;
    private final World world;
    private final List<Resident> residents;
    private Blob home;
    private double x;
    private double y;
    private double r;

    public World(Random random) {
        this.world = this;
        this.home(this);
        this.x = 0;
        this.y = 0;
        this.r = 1;
        this.residents = new LinkedList<>();
        all.add(this);
        this.random = random;
    }

    @Override
    public String toString() {
        return "World";
    }

    public void generateResident() {
        Blob blob = all.get(random.nextInt(all().size()));
        double r = 0.1;
        double d;
        double a;
        double x;
        double y;

        while (true) {
            d = Math.sqrt(random.nextDouble(0, 1)) * (1 - r);
            a = random.nextDouble(0, 2 * Math.PI);
            x = Math.cos(a) * d;
            y = Math.sin(a) * d;
            double finalX = x;
            double finalY = y;
            if (blob.residents().stream().noneMatch(resident -> {
                double dx = resident.x() - finalX;
                double dy = resident.y() - finalY;
                double sr = resident.r() + r;
                return dx * dx + dy * dy < sr * sr;
            })) {
                break;
            }
        }

        new Resident(this, blob, x, y, r);
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public List<Resident> residents() {
        return residents;
    }

    @Override
    public Blob home() {
        return home;
    }

    @Override
    public void home(Blob home) {
        this.home = home;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public void x(double x) {
        this.x = x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public void y(double y) {
        this.y = y;
    }

    @Override
    public double r() {
        return r;
    }

    @Override
    public void r(double r) {
        this.r = r;
    }

    public List<Blob> all() {
        return all;
    }

    public List<Resident> allResidents() {
        return allResidents;
    }
}
