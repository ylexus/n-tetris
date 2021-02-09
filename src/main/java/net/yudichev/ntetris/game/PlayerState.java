package net.yudichev.ntetris.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.ntetris.game.GameConstants.*;
import static net.yudichev.ntetris.game.Scene.ShapeLoweringResult;

final class PlayerState {
    private static final Logger logger = LoggerFactory.getLogger(PlayerState.class);

    /**
     * -1 means no deadline
     */
    private final Player player;
    private final Scene scene;
    private long penaltyDeadline = -1;
    private long lastDropStepTime = -1;
    private long timeSinceLastDrop = -1;
    private ShapeDrop sourceDropWhenTransitioning;
    private long gameTimeMillis = -1;

    PlayerState(Player player, Scene scene) {
        this.player = checkNotNull(player);
        this.scene = checkNotNull(scene);
    }

    @Nullable
    public ShapeDrop getSourceDropWhenTransitioning() {
        return sourceDropWhenTransitioning;
    }

    public void stopTransitioning() {
        sourceDropWhenTransitioning = null;
    }

    public long getTimeSinceLastDrop() {
        return timeSinceLastDrop;
    }

    public void onFrameStart(long gameTimeMillis) {
        this.gameTimeMillis = gameTimeMillis;
        if (lastDropStepTime == -1) {
            lastDropStepTime = gameTimeMillis;
        }
        timeSinceLastDrop = gameTimeMillis - lastDropStepTime;
    }

    public void lowerShape() {
        long outstandingDropSteps = timeSinceLastDrop / DROP_STEP_DURATION;
        logger.debug("{}: {}:  timeSinceLastDrop {}", gameTimeMillis, player, timeSinceLastDrop);
        if (outstandingDropSteps > 0) {
            lastDropStepTime = gameTimeMillis;
            sourceDropWhenTransitioning = scene.getShapeDropsByPlayer().get(player);
            do {
                logger.debug("{}: {}: outstanding steps {}", gameTimeMillis, player, outstandingDropSteps);
                var loweringResult = scene.lowerShape(player);
                logger.debug("player lowering results {}", loweringResult);
                if (loweringResult != null) {
                    processLoweringResult(loweringResult);
                }
            } while (--outstandingDropSteps > 0);
            timeSinceLastDrop = 0;
        } else {
            // stop transition if needed
            if (timeSinceLastDrop >= DROP_TRANSITION_STEP_DURATION) {
                stopTransitioning();
            }
        }
    }

    public void dropShape() {
        var shapeLoweringResult = scene.dropShape(player);
        if (shapeLoweringResult != null) {
            processLoweringResult(shapeLoweringResult);
        }
    }

    public boolean shouldSpawnNewShape() {
        logger.info("{}: player {} has no shape, deadline {}", gameTimeMillis, player, penaltyDeadline);
        if (penaltyDeadline == -1) {
            return true;
        }
        if (gameTimeMillis >= penaltyDeadline) {
            penaltyDeadline = -1;
            return true;
        }
        return false;
    }

    private void processLoweringResult(ShapeLoweringResult shapeLoweringResult) {
        if (shapeLoweringResult == ShapeLoweringResult.REACHED_BOTTOM) {
            penaltyDeadline = gameTimeMillis + PLAYER_PENALTY_PAUSE;
            logger.info("Player {} reached bottom, penalty until {}", player, penaltyDeadline);
        }
    }
}
