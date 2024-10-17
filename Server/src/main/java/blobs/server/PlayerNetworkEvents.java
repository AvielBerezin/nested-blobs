package blobs.server;

import blobs.client.received.ClientMovementRequest;

public interface PlayerNetworkEvents {
    void onNewPlayer(SocketPlayer player);

    void onPlayerLeft(SocketPlayer player);

    void onPlayerData(SocketPlayer player, ClientMovementRequest movementRequest);
}
