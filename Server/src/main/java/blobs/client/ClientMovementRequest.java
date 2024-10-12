package blobs.client;

public record ClientMovementRequest(double angle, double strength) {
    public ClientMovementRequest(double angle, double strength) {
        this.angle = angle;
        this.strength = Math.min(1, Math.max(0, strength));
    }
}
