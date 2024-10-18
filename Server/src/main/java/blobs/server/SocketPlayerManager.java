package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.client.received.ClientZoomRequest;
import blobs.server.network.NetworkListener.Connection;
import blobs.world.Resident;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class SocketPlayerManager {
    private final Map<Connection, SocketPlayer> players;
    private final World world;

    public SocketPlayerManager(World world) {
        this.world = world;
        players = new HashMap<>();
    }

    public Map<Connection, SocketPlayer> players() {
        return players;
    }

    public void sendBlobsData() {
        LinkedList<Connection> closed = new LinkedList<>();
        players().forEach((conn, player) -> {
            try {
                conn.sendData(JSONSerializer.mapper.writeValueAsString(player.blob().pivoted().clientView(player.zoom())));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (WebsocketNotConnectedException e) {
                closed.add(conn);
                System.out.println("encountered closed socket, relevant resources would be closed");
                e.printStackTrace();
            }
        });
        closed.forEach(this::abnormalClose);
    }

    private void remove(Connection conn, int closeStatus) {
        Optional.ofNullable(players().remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
        conn.close(closeStatus);
    }

    public void abnormalClose(Connection conn) {
        synchronized (world) {
            InetSocketAddress address = conn.getRemoteSocketAddress();
            Resident resident = players().get(conn).blob();
            remove(conn, CloseFrame.ABNORMAL_CLOSE);
            System.out.println("server abnormally closed " + address + " of " + resident);
        }
    }

    public void acceptMovementRequest(Connection conn, ClientMovementRequest clientMovementRequest) {
        synchronized (world) {
            players().get(conn).speed(clientMovementRequest.toPoint().multiply(0.01));
        }
    }

    public void acceptZoomRequest(Connection connection, ClientZoomRequest clientZoomRequest) {
        synchronized (world) {
            players.get(connection).zoom(clientZoomRequest.zoom());
        }
    }

    public void playerDisconnected(Connection conn) {
        synchronized (world) {
            Optional.ofNullable(players().remove(conn))
                    .map(SocketPlayer::blob)
                    .ifPresent(Resident::detach);
        }
    }

    public void closeEaten(SocketPlayer player) {
        synchronized (world) {
            Resident resident = player.blob();
            remove(player.connection(), CloseFrame.NORMAL);
            System.out.println("closed " + player.connection().getRemoteSocketAddress() + " of eaten " + resident);
        }
    }

    public SocketPlayer generatePlayer(Connection conn) {
        synchronized (world) {
            SocketPlayer player = new SocketPlayer(this.world.generateResident(true, () -> closeEaten(players().get(conn))), conn);
            players().put(conn, player);
            return player;
        }
    }
}