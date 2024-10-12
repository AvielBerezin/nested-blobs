package blobs.server;

import blobs.client.ClientMovementRequest;
import blobs.world.Resident;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server extends WebSocketServer {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final World world;
    private final Map<WebSocket, Resident> residents;
    private final ScheduledExecutorService scheduler;
    private final Map<WebSocket, Double> xSpeed;
    private final Map<WebSocket, Double> ySpeed;

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
        world = new World(new Random(0));
        for (int i = 0; i < 20; i++) {
            world.generateResident();
        }
        residents = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::mainLoop,
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
        xSpeed = new HashMap<>();
        ySpeed = new HashMap<>();
    }

    private synchronized void mainLoop() {
        try {
            residents.forEach((conn, resident) -> {
                resident.x(resident.x() + xSpeed.get(conn));
                resident.y(resident.y() + ySpeed.get(conn));
            });
            residents.forEach((conn, resident) -> {
                try {
                    conn.send(mapper.writeValueAsString(resident.clientView(20)));
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
        xSpeed.put(conn, 0d);
        ySpeed.put(conn, 0d);
    }

    @Override
    public synchronized void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        residents.remove(conn);
        xSpeed.remove(conn);
        ySpeed.remove(conn);
    }

    @Override
    public synchronized void onMessage(WebSocket conn, String message) {
        try {
            ClientMovementRequest clientMovementRequest = mapper.readValue(message, ClientMovementRequest.class);
            System.out.println(clientMovementRequest);
            xSpeed.put(conn, 0.03 * Math.cos(clientMovementRequest.angle()) * clientMovementRequest.strength());
            ySpeed.put(conn, 0.03 * Math.sin(clientMovementRequest.angle()) * clientMovementRequest.strength());
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

    public static void main(String[] args) {
        WebSocketServer server = new Server(new InetSocketAddress("localhost", 80));
        server.run();
    }
}
