package blobs.server;

import blobs.world.Resident;
import blobs.world.point.Polar;
import org.java_websocket.WebSocket;

public class SocketPlayer extends Player {
    private final WebSocket socket;

    public SocketPlayer(Resident blob, WebSocket socket) {
        super(blob);
        this.socket = socket;
        this.speed(Polar.zero);
    }

    public WebSocket socket() {
        return socket;
    }
}
