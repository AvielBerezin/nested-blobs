package blobs.world;

import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;

public final class Resident extends Blob {
    private final World world;
    private final int id;
    private Blob home;

    public Resident(World world,
                    Blob home,
                    Point2D position,
                    double r) {
        super(position, r);
        this.world = world;
        this.home(home);
        world.all().add(this);
        world.allResidents().add(this);
        home.residents().add(this);
        id = counter++;
    }

    public void consume(Resident food) {
        if (food.home != this.home) {
            throw new RuntimeException("eating is only allowed between residents that share a home." +
                                       " with %s::%s eating %s::%s".formatted(this.home, this,
                                                                              food.home(), food));
        }
        food.home = this;
        this.home.residents().remove(food);
        food.position(food.position().asCartesian().add(this.position().negate().asCartesian()));
        double foodR = food.r();
        double initialR = r();
        food.r(foodR / initialR);
        food.residents().forEach(Resident::leaveHome);
        r(Math.sqrt(initialR * initialR + foodR * foodR));
        residents().forEach(resident -> resident.position(resident.position().asCartesian().multiply(initialR / r())));
    }

    public void leaveHome() {
        Blob leftHome = home();
        this.home = home.home();
        home.residents().add(this);
        this.r(leftHome.r() * r());
        position(leftHome.position().asCartesian().add(position().multiply(leftHome.r()).asCartesian()));
    }

    private static int counter = 0;

    @Override
    public String toString() {
        int lvl = 0;
        Blob blob = this;
        while (blob != blob.home()) {
            blob = blob.home();
            lvl++;
        }
        Cartesian cartesian = position().asCartesian();
        return "Resident(" + id + ")[" +
               "lvl=" + lvl + ", " +
               "x=" + cartesian.x() + ", " +
               "y=" + cartesian.y() + ", " +
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

    public boolean encloses(Resident food) {
        double dr = r() - food.r();
        return position().asCartesian().add(food.position().negate().asCartesian()).squared() < dr * dr;
    }
}
