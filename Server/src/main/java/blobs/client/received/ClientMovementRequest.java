package blobs.client.received;

import blobs.world.point.Polar;

public record ClientMovementRequest(double angle, double strength) {
    public ClientMovementRequest(double angle, double strength) {
        this.angle = angle;
        this.strength = Math.min(1, Math.max(0, strength));
    }

    public Polar toPoint() {
        return Polar.of(angle, strength);
    }
}
