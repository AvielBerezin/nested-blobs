package blobs.server;

import blobs.world.Resident;
import blobs.world.point.Point2D;
import blobs.world.point.Polar;

public class Player {
    private final Resident blob;
    private Point2D speed;

    public Player(Resident blob) {
        this.blob = blob;
        this.speed = Polar.zero;
    }

    public Resident blob() {
        return blob;
    }

    public Point2D speed() {
        return speed;
    }

    public void speed(Point2D speed) {
        this.speed = speed;
    }
}
