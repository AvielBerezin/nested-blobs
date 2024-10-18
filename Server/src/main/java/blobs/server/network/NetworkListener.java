package blobs.server.network;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface NetworkListener {
    interface ConnectionListener {
        void onReceivedData(String data);
        void onConnectionLost();
        void onConnectionCorrupted();
    }

    interface Connection {
        void sendData(String data);
        InetSocketAddress getRemoteSocketAddress();
        void close(int closeStatus);
    }

    void onNewConnection(BiConsumer<ConnectionListener, Consumer<Connection>> connectionListenerContinuation);
}
