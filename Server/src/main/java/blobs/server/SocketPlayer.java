package blobs.server;

import blobs.world.Resident;
import blobs.world.point.Point2D;
import blobs.world.point.Polar;
import org.java_websocket.WebSocket;

public class SocketPlayer {
    private final Resident blob;
    private Point2D speed;
    private final WebSocket socket;

    public SocketPlayer(Resident blob, WebSocket socket) {
        this.blob = blob;
        this.socket = socket;
        this.speed(Polar.zero);
    }

    public Resident blob() {
        return blob;
    }

    public Point2D speed() {
        return speed;
    }

    public WebSocket socket() {
        return socket;
    }

    public void speed(Point2D speed) {
        this.speed = speed;
    }


}
