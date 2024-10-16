package blobs.world.point;

public record Cartesian(double x,
                        double y) implements Point2D {
    public static final Cartesian zero = Cartesian.of(0, 0);

    public static Cartesian of(double x,
                               double y) {
        return new Cartesian(x, y);
    }

    public Polar toPolar() {
        double angle = Math.atan(y / x);
        if (x < 0) {
            angle += Math.PI;
        }
        double distance = Math.sqrt(squared());
        return Polar.of(angle, distance);
    }

    @Override
    public Cartesian negate() {
        return Cartesian.of(-x, -y);
    }

    public Cartesian add(Cartesian position) {
        return Cartesian.of(x + position.x, y + position.y);
    }

    @Override
    public Cartesian multiply(double factor) {
        return Cartesian.of(x * factor, y * factor);
    }

    public double squared() {
        return x * x + y * y;
    }

    @Override
    public <Res> Res dispatch(Point2DDispatcher<Res, Res> dispatcher) {
        return dispatcher.with(this);
    }
}
