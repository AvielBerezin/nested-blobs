package blobs.client.sent;

import java.util.List;

public record ClientView(double radius,
                         ClientBlob player,
                         List<ClientBlob> blobs) {
}
