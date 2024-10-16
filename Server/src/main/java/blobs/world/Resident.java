package blobs.world;

import java.util.ListIterator;

public final class Resident extends Blob {
    private final World world;
    private ListIterator<Resident> residency;
    private Blob home;

    public Resident(World world,
                    Blob home,
                    Position position,
                    double r) {
        super(position, r);
        this.world = world;
        this.home(home);
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
        this.r(leftHome.r() * r());
        position(leftHome.position().add(position().multiply(leftHome.r())));
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
               "x=" + position().x() + ", " +
               "y=" + position().y() + ", " +
               "r=" + r() + ']';
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
}
