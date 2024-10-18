package blobs.server;

import blobs.server.network.WebSocketServerForNetworkListener;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        WebSocketServerForNetworkListener server = new WebSocketServerForNetworkListener(new InetSocketAddress(80), new BlobsOverTheNetwork());
        server.run();
    }
}
