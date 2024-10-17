package blobs.server;

import blobs.client.received.ClientMovementRequest;
import blobs.world.Blob;
import blobs.world.Resident;
import blobs.world.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class Server extends WebSocketServer implements AutoCloseable {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final World world;
    private final Map<WebSocket, SocketPlayer> players;
    private final ScheduledExecutorService scheduler;

    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
        world = new World(new Random(2));
        players = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::mainLoop,
                                      0,
                                      1000 / 30,
                                      TimeUnit.MILLISECONDS);
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
        players.forEach((conn, player) -> {
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
        players.forEach((conn, player) -> {
            try {
                conn.send(mapper.writeValueAsString(player.blob().pivoted().clientView(8)));
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

    @Override
    public synchronized void onOpen(WebSocket conn, ClientHandshake handshake) {
        Resident resident = world.generateResident(() -> initiateEatenClose(players.get(conn).socket()));
        SocketPlayer player = new SocketPlayer(resident, conn);
        System.out.println("new connection to " + conn.getRemoteSocketAddress() + " of " + resident);
        players.put(conn, player);
    }

    @Override
    public synchronized void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (remote) {
            System.out.println("closed " + conn.getRemoteSocketAddress() + " of " + players.get(conn) + " with exit code " + code + " additional info: " + reason);
            remove(conn, code);
        }
    }

    private void initiateAbnormalClose(WebSocket conn) {
        InetSocketAddress address = conn.getRemoteSocketAddress();
        Resident resident = players.get(conn).blob();
        remove(conn, CloseFrame.ABNORMAL_CLOSE);
        System.out.println("server abnormally closed " + address + " of " + resident);
    }

    private void initiateEatenClose(WebSocket conn) {
        InetSocketAddress address = conn.getRemoteSocketAddress();
        Resident resident = players.get(conn).blob();
        remove(conn, CloseFrame.NORMAL);
        System.out.println("server closed " + address + " of eaten " + resident);
    }


    private void remove(WebSocket conn, int closeStatus) {
        Optional.ofNullable(players.remove(conn))
                .map(SocketPlayer::blob)
                .ifPresent(Resident::detach);
        conn.close(closeStatus);
    }

    @Override
    public synchronized void onMessage(WebSocket conn, String message) {
        try {
            ClientMovementRequest clientMovementRequest = mapper.readValue(message, ClientMovementRequest.class);
            players.get(conn).speed(clientMovementRequest.toPoint().multiply(0.01));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            initiateAbnormalClose(conn);
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

    public static void main(String[] args) throws InterruptedException {
        try (Server server = new Server(new InetSocketAddress("localhost", 80))) {
            server.setDaemon(true);
            server.start();
            server.stopped.await();
        }
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
}
