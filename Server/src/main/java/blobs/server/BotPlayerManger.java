package blobs.server;

import blobs.world.World;
import blobs.world.point.Polar;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class BotPlayerManger implements AutoCloseable {
    private final World world;
    private final Random random;
    private final Set<BotPlayer> all;
    private final ScheduledExecutorService scheduler;

    public BotPlayerManger(World world, Random random) {
        this.world = world;
        this.random = random;
        all = new HashSet<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void updateSpeeds() {
        all.forEach(botPlayer -> {
            Polar polar = botPlayer.speed().asPolar();
            botPlayer.speed(Polar.of(polar.angle() + botPlayer.angularAcceleration(),
                                     Math.max(-0.01, Math.min(0.01, polar.distance() + botPlayer.outwardAcceleration()))));
        });
    }

    public void generateBot() {
        AtomicReference<BotPlayer> playerBox = new AtomicReference<>();
        BotPlayer player = new BotPlayer(world.generateResident(() -> all.remove(playerBox.get())));
        playerBox.set(player);
        all.add(player);
        player.randomizeAcceleration(random);
        scheduleAcceleration(player);
        System.out.println("bot created " + player.blob().nestedToString());
    }

    private void scheduleAcceleration(BotPlayer player) {
        scheduler.schedule(() -> {
            synchronized (world) {
                if (all.contains(player)) {
                    player.randomizeAcceleration(random);
                    scheduleAcceleration(player);
                }
            }
        }, random.nextInt(6_000), TimeUnit.MILLISECONDS);
    }

    public Set<BotPlayer> all() {
        return all;
    }

    @Override
    public void close() {
        scheduler.close();
    }
}
