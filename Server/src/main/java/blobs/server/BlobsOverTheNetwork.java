package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.server.network.NetworkListener;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlobsOverTheNetwork implements NetworkListener {
    private final BlobsPhysicsManager blobsPhysicsManager;
    private final ScheduledExecutorService scheduler;
    private final SocketPlayerManager socketPlayerManager;
    private final BotPlayerManger botPlayerManger;

    public BlobsOverTheNetwork() {
        Random random = new Random(0);
        World world = new World(random);
        socketPlayerManager = new SocketPlayerManager(world);
        botPlayerManger = new BotPlayerManger(world, random);
        blobsPhysicsManager = new BlobsPhysicsManager(world, socketPlayerManager, botPlayerManger);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
                                          try {
                                              blobsPhysicsManager.step();
                                          } catch (Exception e) {
                                              e.printStackTrace();
                                              scheduler.shutdown();
                                          }
                                      },
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
    }

    @Override
    public void onNewConnection(BiConsumer<ConnectionListener, Consumer<Connection>> connectionListenerContinuation) {
        AtomicReference<Connection> connectionRef = new AtomicReference<>();
        connectionListenerContinuation.accept(new ConnectionListener() {
            @Override
            public void onReceivedData(String data) {
                ClientMovementRequest clientMovementRequest;
                try {
                    clientMovementRequest = JSONSerializer.mapper.readValue(data, ClientMovementRequest.class);
                    socketPlayerManager.acceptMovementRequest(connectionRef.get(), clientMovementRequest);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    socketPlayerManager.abnormalClose(connectionRef.get());
                }
            }

            @Override
            public void onConnectionLost() {
                socketPlayerManager.playerDisconnected(connectionRef.get());
            }

            @Override
            public void onConnectionCorrupted() {
                socketPlayerManager.playerDisconnected(connectionRef.get());
            }
        }, connectionRef::set);
        System.out.println("new connection to " + connectionRef.get().getRemoteSocketAddress() + " of " + socketPlayerManager.generatePlayer(connectionRef.get()).blob().nestedToString());
    }
}
