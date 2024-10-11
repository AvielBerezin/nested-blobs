package blobs.world;

import java.util.*;

public class World {
    public static final List<World> all = new ArrayList<>(256);
    public static final World world = new World();
    public World home;
    public final List<Blob> residents;

    public double x;
    public double y;
    public double r;

    private World() {
        this.x = 0;
        this.y = 0;
        this.r = 1;
        residents = new LinkedList<>();
        this.home = this;
        all.add(this);
    }

    protected World(double x, double y, double r, World home) {
        this.x = x;
        this.y = y;
        this.r = r;
        residents = new LinkedList<>();
        this.home = home;
        all.add(this);
    }

    public static void main(String[] args) {
        Random random = new Random(0);
        for (int i = 0; i < 9; i++) {
            Blob.generateBlob(random);
        }
        Blob.all.forEach(System.out::println);
    }

    public static final class Blob extends World {

        public static void generateBlob(Random random) {
            World blob = all.get(random.nextInt(all.size()));
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
                double finalY = y;
                double finalX = x;
                if (blob.residents.stream().noneMatch(resident -> {
                    double dx = resident.x - finalX;
                    double dy = resident.y - finalY;
                    double sr = resident.r + r;
                    return dx * dx + dy * dy < sr * sr;
                })) {
                    break;
                }
            }

            new Blob(x, y, r, blob);
        }

        public ListIterator<Blob> residency;


        public Blob(double x, double y, double r, World home) {
            super(x, y, r, home);
            this.home.residents.add(this);
            residency = this.home.residents.listIterator(this.home.residents.size() - 1);
        }

        public void leaveHome() {
            World leftHome = home;
            home = home.home;
            residency.remove();
            home.residents.add(this);
            residency = home.residents.listIterator(home.residents.size() - 1);
            r = leftHome.r * r;
            x = leftHome.x + x * leftHome.r;
            y = leftHome.y + y * leftHome.r;
        }

        public List<ClientBlob> clientView(double viewFactor) {
            List<ClientBlob> result = new LinkedList<>();
            double viewR = r * viewFactor;
            if (home.home != home) {
                for (Blob house : home.home.residents) {
                    if (house != home) {
                        double houseRelX = house.x - home.x;
                        double houseRelY = house.y - home.y;
                        double houseRelR = house.r / home.r;

                        double dx = houseRelX - x;
                        double dy = houseRelY - y;
                        double sr = houseRelR + viewR;

                        if (dx * dx + dy * dy < sr * sr) {
                            result.add(new ClientBlob(dx, dy, houseRelR));
                        }
                    }
                }
            }
            result.add(new ClientBlob(-x, -y, 1));
            for (Blob blob : home.residents) {
                if (blob != this) {
                    double dx = blob.x - x;
                    double dy = blob.y - y;
                    double sr = blob.r + viewR;

                    if (dx * dx + dy * dy < sr * sr) {
                        result.add(new ClientBlob(dx, dy, blob.r));
                    }
                }
            }
            result.add(new ClientBlob(0, 0, r));
            return result;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, r);
        }

        @Override
        public String toString() {
            int lvl = 0;
            World world = home;
            while (world != world.home) {
                world = world.home;
                lvl++;
            }
            return "Blob[" +
                    "lvl=" + lvl + ", " +
                    "x=" + x + ", " +
                    "y=" + y + ", " +
                    "r=" + r + ']';
        }
    }

    @Override
    public String toString() {
        return "TheWorld";
    }
}
