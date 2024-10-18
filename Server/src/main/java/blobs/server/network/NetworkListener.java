package blobs.server.network;

import java.net.InetSocketAddress;
import java.util.function.Function;

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

    void onNewConnection(Function<ConnectionListener, Connection> connector);
}
