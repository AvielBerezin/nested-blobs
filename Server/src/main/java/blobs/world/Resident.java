package blobs.world;

import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;

import java.util.ArrayList;
import java.util.Optional;

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
        this.home = home;
        world.all().add(this);
        world.allResidents().add(this);
        home.residents().add(this);
        id = counter++;
    }

    public void consume(Resident food) {
        if (food.home()
                .map(foodHome -> this.home()
                                     .map(thisHome -> foodHome != thisHome)
                                     .orElse(false))
                .orElse(this.home().isPresent())) {
            throw new RuntimeException("eating is only allowed between residents that share a home." +
                                       " with %s::%s eating %s::%s".formatted(this.home(), this,
                                                                              food.home(), food));
        }
        String thisDescription = this.nestedToString();
        String foodDescription = food.nestedToString();
        double newSize = Math.sqrt(r() * r() + food.r() * food.r());
        swallow(food);
        food.detach();
        resize(newSize);
        String eatenDescription = food.nestedToString();
        System.out.println(thisDescription + " ate " + foodDescription + " into " + eatenDescription);
    }

    private void swallow(Resident food) {
        food.home().map(Blob::residents).ifPresent(residents -> residents.remove(food));
        food.home = this;
        food.home().map(Blob::residents).ifPresent(residents -> residents.add(food));
        food.position(food.position().asCartesian().add(this.position().negate().asCartesian()));
        food.r(food.r() / r());
    }

    private void resize(double newRadius) {
        double initialR = r();
        r(newRadius);
        residents().forEach(resident -> resident.position(resident.position().asCartesian().multiply(initialR / r())));
    }

    public void leaveHome() {
        Optional<Blob> leftHome = home();
        this.home = home().flatMap(Blob::home).orElse(null);
        leftHome.ifPresent(home -> home.residents().remove(this));
        home().ifPresent(home -> home.residents().add(this));
        leftHome.ifPresent(home -> this.r(home.r() * r()));
        leftHome.ifPresent(home -> position(home.position().asCartesian().add(position().multiply(home.r()).asCartesian())));
    }

    private static int counter = 0;

    @Override
    public String toString() {
        Cartesian cartesian = position().asCartesian();
        return "Resident(" + id + ")[" +
               "lvl=" + level() + ", " +
               "x=" + cartesian.x() + ", " +
               "y=" + cartesian.y() + ", " +
               "r=" + r() + ']';
    }

    @Override
    public String nestedToString() {
        return home().map(Blob::nestedToString).map(str -> str + "::").orElse("") + "Resident(" + id + ")";
    }

    @Override
    public Optional<Blob> home() {
        return Optional.ofNullable(home);
    }

    public boolean encloses(Resident food) {
        double dr = r() - food.r();
        return position().asCartesian().add(food.position().negate().asCartesian()).squared() < dr * dr;
    }

    public void detach() {
        new ArrayList<>(residents()).forEach(Resident::leaveHome);
        this.world.all().remove(this);
        this.world.allResidents().remove(this);
        this.home().map(Blob::residents).ifPresent(residents -> residents.remove(this));
        this.home = null;
    }
}
