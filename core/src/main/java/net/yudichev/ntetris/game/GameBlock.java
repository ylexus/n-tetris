package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.Sprite;
import net.yudichev.ntetris.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

abstract class GameBlock<S extends Shape<S>> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Sprite sprite;
    protected double lastMoveTime = Double.MIN_VALUE;
    protected double timeSinceLastMove;
    protected double gameTime;
    @Nullable
    protected S sourceShapeWhenTransitioning;

    protected GameBlock(Sprite sprite, double creationGameTime) {
        this.sprite = checkNotNull(sprite);
        onFrameStart(creationGameTime);
    }

    @SuppressWarnings("FloatingPointEquality")
    public final void onFrameStart(double gameTime) {
        this.gameTime = gameTime;
        if (lastMoveTime == Double.MIN_VALUE) {
            lastMoveTime = gameTime;
        }
        timeSinceLastMove = gameTime - lastMoveTime;
    }

    public abstract void render(GameCanvas canvas);

    protected void renderShape(GameCanvas canvas, S destinationShape) {
        double transitionProportion = timeSinceLastMove / DROP_TRANSITION_STEP_DURATION;
        logger.debug("render {} src {} dest {}, proportion {}", sprite, sourceShapeWhenTransitioning, destinationShape, transitionProportion);
        for (int patternY = 0; patternY < destinationShape.height(); patternY++) {
            Row row = destinationShape.pattern().getRows().get(patternY);
            for (int patternX = 0; patternX < row.getElements().length; patternX++) {
                if (row.getElements()[patternX]) {
                    double targetAbsX = destinationShape.toAbsoluteX(patternX);
                    double targetAbsY = destinationShape.toAbsoluteY(patternY);
                    if (sourceShapeWhenTransitioning != null) {
                        // extrapolate transition
                        int sourceAbsY = sourceShapeWhenTransitioning.toAbsoluteY(patternY);
                        int sourceAbsX = sourceShapeWhenTransitioning.toAbsoluteX(patternX);
                        logger.debug("render {} trans from {}, {} -> {}, {}", sprite, sourceAbsX, sourceAbsY, targetAbsX, targetAbsY);
                        targetAbsX = sourceAbsX + (targetAbsX - sourceAbsX) * transitionProportion;
                        targetAbsY = sourceAbsY + (targetAbsY - sourceAbsY) * transitionProportion;
                        logger.debug("render {} trans to {}, {}", sprite, targetAbsX, targetAbsY);
                    }
                    logger.debug("render {} block {},{} at {}, {}", sprite, patternX, patternY, targetAbsX, targetAbsY);
                    canvas.renderBlock(targetAbsX, targetAbsY, sprite, 1.0);
                }
            }
        }
    }
}
