package blobs.world;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public final class Resident implements Blob {
    private final World world;
    private final List<Resident> residents;
    private ListIterator<Resident> residency;
    private Blob home;
    private double x;
    private double y;
    private double r;

    public Resident(World world,
                    Blob home,
                    double x,
                    double y,
                    double r) {
        this.world = world;
        this.home(home);
        this.x = x;
        this.y = y;
        this.r = r;
        this.residents = new LinkedList<>();
        world.all().add(this);
        world.allResidents().add(this);
        home.residents().add(this);
        this.residency = this.home().residents().listIterator(this.home().residents().size() - 1);
    }

    public void leaveHome() {
        Blob leftHome = home();
        this.home = home.home();
        residency.remove();
        home.residents().add(this);
        this.residency = home.residents().listIterator(home.residents().size() - 1);
        this.r = leftHome.r() * r();
        this.x = leftHome.x() + x() * leftHome.r();
        this.y = leftHome.y() + y() * leftHome.r();
    }

    public List<ClientBlob> clientView(double viewFactor) {
        List<ClientBlob> result = new LinkedList<>();
        double viewR = r() * viewFactor;
        if (home.home() != home) {
            for (Resident house : home.home().residents()) {
                if (house != home()) {
                    double houseRelX = (house.x() - home.x()) / home.r();
                    double houseRelY = (house.y() - home.y()) / home.r();
                    double houseRelR = house.r() / home.r();

                    double dx = houseRelX - x();
                    double dy = houseRelY - y();
                    double sr = houseRelR + viewR;

                    if (dx * dx + dy * dy < sr * sr) {
                        result.add(new ClientBlob(dx, dy, houseRelR));
                    }
                }
            }
        }
        result.add(new ClientBlob(-x(), -y(), 1));
        for (Resident resident : home.residents()) {
            if (resident != this) {
                double dx = resident.x() - x();
                double dy = resident.y() - y();
                double sr = resident.r() + viewR;

                if (dx * dx + dy * dy < sr * sr) {
                    result.add(new ClientBlob(dx, dy, resident.r()));
                }
            }
        }
        result.add(new ClientBlob(0, 0, r()));
        return result;
    }

    @Override
    public String toString() {
        int lvl = 0;
        Blob blob = this;
        while (blob != blob.home()) {
            blob = blob.home();
            lvl++;
        }
        return "Resident[" +
               "lvl=" + lvl + ", " +
               "x=" + x() + ", " +
               "y=" + y() + ", " +
               "r=" + r() + ']';
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
}
