package blobs.world;

import java.util.Random;

public record Position(double x,
                       double y) {
    public static final Position zero = Position.of(0, 0);

    public static Position of(double x,
                              double y) {
        return new Position(x, y);
    }

    public static Position polar(double angle,
                                 double distance) {
        return Position.of(Math.cos(angle) * distance, Math.sin(angle) * distance);
    }

    public static Position randomInCircle(Random random) {
        double distance = Math.sqrt(random.nextDouble(0, 1));
        double angle = random.nextDouble(0, 2 * Math.PI);
        return polar(angle, distance);
    }

    public Position negate() {
        return Position.of(-x, -y);
    }

    public Position add(Position position) {
        return Position.of(x + position.x, y + position.y);
    }

    public Position multiply(double factor) {
        return Position.of(x * factor, y * factor);
    }

    public double squared() {
        return x * x + y * y;
    }
}
