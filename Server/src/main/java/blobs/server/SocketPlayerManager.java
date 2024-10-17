package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.Resident;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class SocketPlayerManager {
    private final Map<WebSocket, SocketPlayer> players;
    private final World world;

    public SocketPlayerManager(World world) {
        this.world = world;
        players = new HashMap<>();
    }

    public Map<WebSocket, SocketPlayer> players() {
        return players;
    }

    void sendBlobsData() {
        LinkedList<WebSocket> closed = new LinkedList<>();
        players().forEach((conn, player) -> {
            try {
                conn.send(JSONSerializer.mapper.writeValueAsString(player.blob().pivoted().clientView(8)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (WebsocketNotConnectedException e) {
                closed.add(conn);
                System.out.println("encountered closed socket, relevant resources would be closed");
                e.printStackTrace();
            }
        });
        closed.forEach(this::initiateAbnormalClose);
    }

    void remove(WebSocket conn, int closeStatus) {
        Optional.ofNullable(players().remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
        conn.close(closeStatus);
    }

    public void initiateAbnormalClose(WebSocket conn) {
        InetSocketAddress address = conn.getRemoteSocketAddress();
        Resident resident = players().get(conn).blob();
        remove(conn, CloseFrame.ABNORMAL_CLOSE);
        System.out.println("server abnormally closed " + address + " of " + resident);
    }

    public void acceptMovementRequest(WebSocket conn, ClientMovementRequest clientMovementRequest) {
        players().get(conn).speed(clientMovementRequest.toPoint().multiply(0.01));
    }

    public void playerDisconnected(WebSocket conn) {
        Optional.ofNullable(players().remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
    }

    public void initiateEatenClose(SocketPlayer player) {
        Resident resident = player.blob();
        remove(player.socket(), CloseFrame.NORMAL);
        System.out.println("server closed " + player.socket().getRemoteSocketAddress() + " of eaten " + resident);
    }

    public SocketPlayer generatePlayer(WebSocket conn) {
        SocketPlayer player = new SocketPlayer(this.world.generateResident(() -> initiateEatenClose(players().get(conn))), conn);
        players().put(conn, player);
        return player;
    }
}