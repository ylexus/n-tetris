package net.yudichev.ntetris.game;

import net.yudichev.ntetris.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Double.MIN_VALUE;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class GameVariables {
    private static final Logger logger = LoggerFactory.getLogger(GameVariables.class);

    private static final long MINUTE_IN_MILLIS = MINUTES.toMillis(1);
    private final Settings settings;
    private double speedMultiplier = 1.0;
    private double lastAccelerationTime = MIN_VALUE;

    GameVariables(Settings settings) {
        this.settings = checkNotNull(settings);
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void accelerateSpeed(double gameTime) {
        //noinspection FloatingPointEquality as designed
        if (lastAccelerationTime == MIN_VALUE) {
            lastAccelerationTime = gameTime;
            return;
        }
        if (gameTime - lastAccelerationTime > MINUTE_IN_MILLIS) {
            speedMultiplier *= settings.accelerationRatePerMinute();
            if (logger.isInfoEnabled()) {
                logger.info("Speed now {}", speedMultiplier);
            }
            lastAccelerationTime = gameTime;
        }
    }
}
