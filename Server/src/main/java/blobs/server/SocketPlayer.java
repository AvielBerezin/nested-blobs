package blobs.server;

import blobs.server.network.NetworkListener;
import blobs.world.Resident;
import blobs.world.point.Polar;

public class SocketPlayer extends Player {
    private final NetworkListener.Connection connection;

    public SocketPlayer(Resident blob, NetworkListener.Connection connection) {
        super(blob);
        this.connection = connection;
        this.speed(Polar.zero);
    }

    public NetworkListener.Connection connection() {
        return connection;
    }
}
