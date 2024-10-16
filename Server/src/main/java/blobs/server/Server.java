package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.Resident;
import blobs.world.World;
import blobs.world.point.Cartesian;
import blobs.world.point.Point2D;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server extends WebSocketServer implements AutoCloseable {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final World world;
    private final Map<WebSocket, Resident> residents;
    private final ScheduledExecutorService scheduler;
    private final Map<WebSocket, Point2D> speed;

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
        world = new World(new Random(2));
//        for (int i = 0; i < 20; i++) {
//            world.generateResident();
//        }
        residents = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::mainLoop,
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
        speed = new HashMap<>();
    }

    private synchronized void mainLoop() {
        try {
            residents.forEach((conn, resident) -> {
                resident.position(resident.position().asCartesian().add(speed.get(conn).asCartesian()));
            });
            residents.forEach((conn, resident) -> {
                try {
                    conn.send(mapper.writeValueAsString(resident.pivoted().world().clientView(8)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
        residents.put(conn, world.generateResident());
        speed.put(conn, Cartesian.zero);
    }

    @Override
    public synchronized void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        residents.remove(conn);
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

    @Override
    public void close() throws InterruptedException {
        System.out.println("stopping...");
        scheduler.close();
        this.stop();
    }
}
