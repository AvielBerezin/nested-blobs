package blobs.world;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

public class Server extends WebSocketServer {
    static int seed = 4;

    static {
    }

    public static final ObjectMapper mapper = new ObjectMapper();

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Random random = new Random(seed);
        World world = new World(random);
        for (int i = 0; i < 20; i++) {
            world.generateResident();
        }
        List<ClientBlob> clientBlobs = world.allResidents().get(3).clientView(20);
        System.out.println(seed + ": " + clientBlobs.size() + " blobs");
        seed++;

        try {
            conn.send(mapper.writeValueAsString(clientBlobs));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
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
