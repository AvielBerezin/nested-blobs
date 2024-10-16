package blobs.world.point;

import java.util.Random;

public record Polar(double angle,
                    double distance) implements Point2D {
    public static final Polar zero = Polar.of(0, 0);

    public Polar(double angle, double distance) {
        if (distance < 0) {
            this.angle = angle + Math.PI;
            this.distance = -distance;
        } else {
            this.angle = angle;
            this.distance = distance;
        }
    }

    public static Polar of(double angle,
                           double distance) {
        return new Polar(angle, distance);
    }

    public static Polar randomInCircle(Random random) {
        double distance = Math.sqrt(random.nextDouble(0, 1));
        double angle = random.nextDouble(0, 2 * Math.PI);
        return of(angle, distance);
    }

    public Cartesian toCartesian() {
        return Cartesian.of(Math.cos(angle) * distance,
                            Math.sin(angle) * distance);
    }

    @Override
    public Polar negate() {
        return Polar.of(angle + Math.PI, distance);
    }

    @Override
    public Polar multiply(double factor) {
        return Polar.of(angle, distance * factor);
    }

    @Override
    public <Res> Res dispatch(Point2DDispatcher<Res, Res> dispatcher) {
        return dispatcher.with(this);
    }
}
