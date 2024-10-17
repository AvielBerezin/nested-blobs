package blobs.server;

import blobs.world.Resident;

import java.util.Random;

public class BotPlayer extends Player {
    private double angularAcceleration = 0;
    private double outwardAcceleration = 0;

    public BotPlayer(Resident blob) {
        super(blob);
    }

    public void randomizeAcceleration(Random random) {
        angularAcceleration = random.nextDouble(-0.01, 0.01) * Math.PI;
        outwardAcceleration = random.nextDouble(-0.001, 0.001);
    }

    public double angularAcceleration() {
        return angularAcceleration;
    }

    public double outwardAcceleration() {
        return outwardAcceleration;
    }
}
