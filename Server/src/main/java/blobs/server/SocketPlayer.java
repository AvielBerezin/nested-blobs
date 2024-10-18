package blobs.server;

import blobs.server.network.NetworkListener;
import blobs.world.Resident;
import blobs.world.point.Polar;

public class SocketPlayer extends Player {
    private final NetworkListener.Connection connection;
    private double zoom = 12;

    public SocketPlayer(Resident blob, NetworkListener.Connection connection) {
        super(blob);
        this.connection = connection;
        this.speed(Polar.zero);
    }

    public NetworkListener.Connection connection() {
        return connection;
    }

    public void zoom(boolean zoomOut) {
        this.zoom = Math.max(0.5, Math.min(12, zoom() * (zoomOut ? 1.1 : 1 / 1.1)));
    }

    public double zoom() {
        return zoom;
    }
}
