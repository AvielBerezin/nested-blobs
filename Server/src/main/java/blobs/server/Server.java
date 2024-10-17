package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.*;

public class Server extends WebSocketServer implements AutoCloseable {
    private final BlobsPhysicsManager game;
    private final ScheduledExecutorService scheduler;
    private final SocketPlayerManager socketPlayerManager;

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
        World world = new World(new Random(0));
        socketPlayerManager = new SocketPlayerManager(world);
        game = new BlobsPhysicsManager(socketPlayerManager, world);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
                                          try {
                                              game.step();
                                          } catch (Exception e) {
                                              e.printStackTrace();
                                              try {
                                                  internalServerErrorShutDown();
                                              } catch (InterruptedException ex) {
                                                  ex.printStackTrace();
                                              }
                                          }
                                      },
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress() + " of " + socketPlayerManager.generatePlayer(conn).blob());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (remote) {
            System.out.println("closed " + conn.getRemoteSocketAddress() + " of " + socketPlayerManager.players().get(conn) + " with exit code " + code + " additional info: " + reason);
            socketPlayerManager.playerDisconnected(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            ClientMovementRequest clientMovementRequest = JSONSerializer.mapper.readValue(message, ClientMovementRequest.class);
            socketPlayerManager.acceptMovementRequest(conn, clientMovementRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            socketPlayerManager.initiateAbnormalClose(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        conn.close(400, "client should not send ByteBuffer data to server");
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

    CountDownLatch stopped = new CountDownLatch(1);
    ExecutorService inputWaiterExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("waiter-for-initiated-termination-by-stdin-input");
        thread.setUncaughtExceptionHandler((t, e) -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println("thread [" + t.getName() + "] was terminated exceptionally: ");
            e.printStackTrace(printStream);
            try {
                System.err.write(outputStream.toByteArray());
            } catch (IOException ignored) {
            }
        });
        return thread;
    });
    {
        inputWaiterExecutor.submit(() -> {
            try {
                if (System.in.read() == -1) {
                    System.out.println("reached end of input");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                inputWaiterExecutor.shutdown();
                stopped.countDown();
            }
        });
    }

    private void internalServerErrorShutDown() throws InterruptedException {
        System.err.println("something went wrong server side");
        Thread thread = new Thread(() -> getConnections().forEach(webSocket -> webSocket.close(CloseFrame.ABNORMAL_CLOSE)));
        thread.setDaemon(false);
        thread.setName("internal-server-error-shut-down-messenger");
        thread.start();
        System.out.println("stopping...");
        scheduler.shutdown();
        stopped.countDown();
    }

    @Override
    public void close() throws InterruptedException {
        System.out.println("stopping...");
        scheduler.close();
        stop();
    }

    public static void main(String[] args) throws InterruptedException {
        try (Server server = new Server(new InetSocketAddress("localhost", 80))) {
            server.setDaemon(true);
            server.start();
            server.stopped.await();
        }
    }
}
