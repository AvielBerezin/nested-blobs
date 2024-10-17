package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.Blob;
import blobs.world.Resident;
import blobs.world.World;
import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class Server extends WebSocketServer implements AutoCloseable {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final World world;
    private final Map<WebSocket, Resident> residents;
    private final Map<Resident, WebSocket> sockets;
    private final ScheduledExecutorService scheduler;
    private final Map<WebSocket, Point2D> speed;

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
        world = new World(new Random(2));
        residents = new ConcurrentHashMap<>();
        sockets = new HashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::mainLoop,
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
        speed = new HashMap<>();
    }

    private synchronized void mainLoop() {
        try {
            moveEveryone();
            feeding();
            sendBlobsData();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                internalServerErrorShutDown();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void feeding() {
        world.all().forEach(this::houseFeast);
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
            if (isEdible(resident, food)) {
                foods.remove();
                eat(resident, food);
                eaten = true;
            }
        }
        return eaten;
    }

    private void moveEveryone() {
        residents.forEach((conn, resident) -> {
            resident.position(resident.position().asCartesian().add(speed.get(conn).asCartesian()));
            // such an f satisfies ln(f*d+1)/m = 0.5*d for d = 0.4 works
            double f = 6.28215;
            double pushBack = Math.log(f * 2 * resident.r() + 1) / f;
            resident.position(resident.position()
                                      .multiply(Math.max(0, Math.min(1, 1 / (resident.position().asPolar().distance() - resident.r() + pushBack))))
                                      .asCartesian());
        });
    }

    private void sendBlobsData() {
        residents.forEach((conn, resident) -> {
            try {
                conn.send(mapper.writeValueAsString(resident.pivoted().world().clientView(8)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void eat(Resident resident, Resident food) {
        resident.consume(food);
        WebSocket conn = sockets.remove(food);
        this.residents.remove(conn);
        conn.close();
    }

    private static boolean isEdible(Resident resident, Resident food) {
        double radiiRatio = food.r() / resident.r();
        return resident.encloses(food) && radiiRatio < 0.8;
    }

    @Override
    public synchronized void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
        Resident resident = world.generateResident();
        residents.put(conn, resident);
        sockets.put(resident, conn);
        speed.put(conn, Cartesian.zero);
    }

    @Override
    public synchronized void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        Optional.ofNullable(residents.remove(conn)).ifPresent(sockets::remove);
        speed.remove(conn);
    }

    @Override
    public synchronized void onMessage(WebSocket conn, String message) {
        try {
            ClientMovementRequest clientMovementRequest = mapper.readValue(message, ClientMovementRequest.class);
            System.out.println(clientMovementRequest);
            speed.put(conn, clientMovementRequest.toPoint().multiply(0.01));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        conn.close(400, "client should not send data to server");
        System.err.println("client " + conn.getResourceDescriptor() + " sent data.");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        try (Server server = new Server(new InetSocketAddress("localhost", 80))) {
            server.setDaemon(true);
            server.start();
            if (System.in.read() == -1) {
                System.out.println("reached end of input");
            }
        }
    }

    private void internalServerErrorShutDown() throws InterruptedException {
        System.err.println("something went wrong server side");
        scheduler.close();
        getConnections().forEach(webSocket -> webSocket.close(CloseFrame.ABNORMAL_CLOSE));
        stop();
    }

    @Override
    public void close() throws InterruptedException {
        System.out.println("stopping...");
        scheduler.close();
        this.stop();
    }
}
