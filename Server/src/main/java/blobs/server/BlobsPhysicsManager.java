package blobs.server;

import blobs.world.Blob;
import blobs.world.Resident;
import blobs.world.World;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListSet;

public class BlobsPhysicsManager {
    private final World world;
    private final SocketPlayerManager socketPlayerManager;
    private final BotPlayerManger botPlayerManger;
    private Instant lastGenerated;

    public BlobsPhysicsManager(World world, SocketPlayerManager socketPlayerManager, BotPlayerManger botPlayerManger) {
        this.world = world;
        this.socketPlayerManager = socketPlayerManager;
        this.botPlayerManger = botPlayerManger;
    }

    public void step() throws Exception {
        synchronized (world) {
            try {
                botPlayerManger.updateSpeeds();
                moveEveryone();
                feeding();
                socketPlayerManager.sendBlobsData();
                if (world.all().size() < 20 &&
                    (lastGenerated == null ||
                     Duration.between(lastGenerated, Instant.now())
                             .compareTo(Duration.ofSeconds(2))
                     > 0)) {
                    botPlayerManger.generateBot();
                    lastGenerated = Instant.now();
                }
            } catch (Throwable e) {
                throw new Exception(e);
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
        LinkedList<Player> players = new LinkedList<>();
        players.addAll(socketPlayerManager.players().values());
        players.addAll(botPlayerManger.all());
        players.forEach(player -> {
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
}
