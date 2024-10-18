package blobs.server.network;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketServerForNetworkListener extends WebSocketServer implements AutoCloseable {
    private final NetworkListener listener;
    private final Map<WebSocket, WebSocketConnection> connections;
    private final KeyedExecutor<WebSocket> readExecutor;
    private final KeyedExecutor<WebSocket> writeExecutor;

    public WebSocketServerForNetworkListener(InetSocketAddress inetSocketAddress, NetworkListener listener) {
        super(inetSocketAddress);
        this.listener = listener;
        connections = new ConcurrentHashMap<>();
        readExecutor = new KeyedExecutor<>(4);
        writeExecutor = new KeyedExecutor<>(4);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        AtomicBoolean executed = new AtomicBoolean(false);
        WebSocketConnection connection = connections.computeIfAbsent(conn, WebSocketConnection::new);
        readExecutor.execute(conn, () -> {
            listener.onNewConnection((connectionListener) -> {
                if (!executed.getAndSet(true)) {
                    connection.listener(connectionListener);
                }
                return connection;
            });
            if (!executed.get()) {
                connections.remove(conn);
                connection.listener.onConnectionCorrupted();
                conn.close(CloseFrame.ABNORMAL_CLOSE);
            }
        });
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (remote) {
            readExecutor.execute(conn, () -> {
                WebSocketConnection connection = connections.get(conn);
                if (connection != null) {
                    connection.listener.onConnectionLost();
                }
            });
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        readExecutor.execute(conn, () -> {
            WebSocketConnection connection = connections.get(conn);
            if (connection != null) {
                connection.listener.onReceivedData(message);
            }
        });
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        readExecutor.execute(conn, () -> {
            WebSocketConnection connection = connections.get(conn);
            if (connection != null) {
                connection.listener.onConnectionCorrupted();
                connections.remove(conn);
            }
            writeExecutor.execute(conn, () -> {
                if (conn.isOpen()) {
                    conn.close(CloseFrame.ABNORMAL_CLOSE);
                }
            });
        });
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn == null) {
            ex.printStackTrace();
        } else {
            readExecutor.execute(conn, () -> {
                WebSocketConnection connection = connections.get(conn);
                if (connection != null) {
                    connection.listener.onConnectionCorrupted();
                    connections.remove(conn);
                    ex.printStackTrace();
                    writeExecutor.execute(conn, () -> {
                        if (conn.isOpen()) {
                            conn.close(CloseFrame.ABNORMAL_CLOSE);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void close() throws InterruptedException {
        stop();
    }

    public static void main(String[] args) throws InterruptedException {
        try (WebSocketServerForNetworkListener server = new WebSocketServerForNetworkListener(new InetSocketAddress("localhost", 80), connectionListenerContinuation -> {
            System.out.println("new connection!");
            NetworkListener.ConnectionListener connectionListener = new NetworkListener.ConnectionListener() {
                @Override
                public void onReceivedData(String data) {
                    System.out.println("received " + data);
                }

                @Override
                public void onConnectionLost() {
                    System.out.println("connection lost");
                }

                @Override
                public void onConnectionCorrupted() {
                    System.out.println("connection corrupted");
                }
            };
            connectionListenerContinuation.apply(connectionListener);
        })) {
            server.setDaemon(true);
            server.run();
        }
    }

    private class WebSocketConnection implements NetworkListener.Connection {
        private NetworkListener.ConnectionListener listener;
        private final WebSocket socket;

        public WebSocketConnection(WebSocket socket) {
            this.socket = socket;
        }

        public void listener(NetworkListener.ConnectionListener listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
        }

        @Override
        public synchronized void sendData(String data) {
            writeExecutor.execute(socket, () -> {
                if (connections.containsKey(socket)) {
                    if (socket.isOpen()) {
                        socket.send(data);
                    } else {
                        connections.remove(socket);
                        readExecutor.execute(socket, listener::onConnectionCorrupted);
                    }
                }
            });
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return socket.getRemoteSocketAddress();
        }

        @Override
        public void close(int code) {
            writeExecutor.execute(socket, () -> {
                if (socket.isOpen()) {
                    socket.close(code);
                }
                connections.remove(socket);
            });
        }
    }
}
