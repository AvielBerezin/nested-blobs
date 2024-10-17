package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.Blob;
import blobs.world.Resident;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Game {
    private final World world;
    private final Map<WebSocket, SocketPlayer> players;

    public Game() {
        world = new World(new Random(0));
        players = new ConcurrentHashMap<>();
    }

    public Map<WebSocket, SocketPlayer> players() {
        return players;
    }

    public void mainLoop() throws Exception {
        try {
            moveEveryone();
            feeding();
            sendBlobsData();
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void feeding() {
        ArrayList<Blob> allSorted = new ArrayList<>(world.all());
        allSorted.sort(Comparator.comparing(Blob::level).reversed());
        allSorted.forEach(this::houseFeast);
    }

    private void houseFeast(Blob house) {
        ConcurrentSkipListSet<Resident> orderedResidents = new ConcurrentSkipListSet<>(Comparator.<Blob, Double>comparing(Blob::r).reversed());
        orderedResidents.addAll(house.residents());
        Iterator<Resident> residents = orderedResidents.iterator();
        while (residents.hasNext()) {
            Resident resident = residents.next();
            residents.remove();
            boolean eaten = residentEat(orderedResidents, resident);
            orderedResidents.add(resident);
            if (eaten) {
                residents = orderedResidents.tailSet(resident, true).iterator();
            }
        }
    }

    private boolean residentEat(ConcurrentSkipListSet<Resident> orderedResidents, Resident excludedResident) {
        boolean eaten = false;
        boolean lastEaten;
        do {
            lastEaten = residentEatOneTraversal(orderedResidents.tailSet(excludedResident, false).iterator(), excludedResident);
            eaten = eaten || lastEaten;
        } while (lastEaten);
        return eaten;
    }

    private boolean residentEatOneTraversal(Iterator<Resident> foods, Resident resident) {
        boolean eaten = false;
        while (foods.hasNext()) {
            Resident food = foods.next();
            if (resident.canEat(food)) {
                foods.remove();
                resident.eat(food);
                eaten = true;
            }
        }
        return eaten;
    }

    private void moveEveryone() {
        players().forEach((conn, player) -> {
            Resident resident = player.blob();
            resident.position(resident.position().asCartesian().add(player.speed().asCartesian()));
            // such an f satisfies ln(f*d+1)/m = 0.5*d for d = 0.4 works
            // meaning that starting with a radius of d/2 = 0.2, a blob can go halfway out of the border.
            double f = 6.28215;
            double pushBack = Math.log(f * 2 * resident.r() + 1) / f;
            resident.position(resident.position()
                                      .multiply(Math.max(0d, Math.min(1d, 1d / Math.max(+0d, resident.position().asPolar().distance() - resident.r() + pushBack))))
                                      .asCartesian());
            if (resident.position().asPolar().distance() > 1 &&
                resident.home().isPresent() &&
                resident.home().get().home().isPresent()) {
                resident.leaveHome();
            }
        });
    }

    private void sendBlobsData() {
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

    private void remove(WebSocket conn, int closeStatus) {
        Optional.ofNullable(players().remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
        conn.close(closeStatus);
    }

    public void initiateEatenClose(SocketPlayer player) {
        Resident resident = player.blob();
        remove(player.socket(), CloseFrame.NORMAL);
        System.out.println("server closed " + player.socket().getRemoteSocketAddress() + " of eaten " + resident);
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

    public SocketPlayer generatePlayer(WebSocket conn) {
        SocketPlayer player = new SocketPlayer(world.generateResident(() -> initiateEatenClose(players().get(conn))), conn);
        players().put(conn, player);
        return player;
    }

    public void playerDisconnected(WebSocket conn) {
        Optional.ofNullable(players().remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
    }
}
