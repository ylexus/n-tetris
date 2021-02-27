package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

abstract class GameShape {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Block block;
    protected final Scene scene;
    protected final GameCanvas canvas;
    protected double lastMoveTime = Double.MIN_VALUE;
    protected double timeSinceLastMove;
    protected double gameTime;
    @Nullable
    protected Shape sourceShapeWhenTransitioning;

    protected GameShape(Scene scene, GameCanvas canvas, Block block) {
        this.scene = checkNotNull(scene);
        this.canvas = checkNotNull(canvas);
        this.block = checkNotNull(block);
    }

    @SuppressWarnings("FloatingPointEquality")
    public void onFrameStart(double gameTimeMillis) {
        gameTime = gameTimeMillis;
        if (lastMoveTime == Double.MIN_VALUE) {
            lastMoveTime = gameTimeMillis;
        }
        timeSinceLastMove = gameTimeMillis - lastMoveTime;
    }

    public abstract void render();

    protected void renderShape(Shape destinationShape) {
        double transitionProportion = timeSinceLastMove / DROP_TRANSITION_STEP_DURATION;
        logger.debug("render {} src {} dest {}, proportion {}", block, sourceShapeWhenTransitioning, destinationShape, transitionProportion);
        for (int rowIdx = 0; rowIdx < destinationShape.pattern().getRows().size(); rowIdx++) {
            Row row = destinationShape.pattern().getRows().get(rowIdx);
            for (int colIdx = 0; colIdx < row.getElements().length; colIdx++) {
                if (row.getElements()[colIdx]) {
                    double targetAbsRowIdx = rowIdx + destinationShape.horizontalOffset();
                    double targetAbsColIdx = colIdx + destinationShape.verticalOffset();
                    if (sourceShapeWhenTransitioning != null) {
                        // extrapolate transition
                        int sourceAbsRowIdx = rowIdx + sourceShapeWhenTransitioning.horizontalOffset();
                        int sourceAbsColIdx = colIdx + sourceShapeWhenTransitioning.verticalOffset();
                        logger.debug("render {} trans from {}, {} -> {}, {}", block, sourceAbsRowIdx, sourceAbsColIdx, targetAbsRowIdx, targetAbsColIdx);
                        targetAbsRowIdx = sourceAbsRowIdx + (targetAbsRowIdx - sourceAbsRowIdx) * transitionProportion;
                        targetAbsColIdx = sourceAbsColIdx + (targetAbsColIdx - sourceAbsColIdx) * transitionProportion;
                        logger.debug("render {} trans to {}, {}", block, targetAbsRowIdx, targetAbsColIdx);
                    }
                    logger.debug("render {} block {},{} at {}, {}", block, rowIdx, colIdx, targetAbsRowIdx, targetAbsColIdx);
                    canvas.renderBlock(scene.blockToAbsHorizontal(targetAbsRowIdx), scene.blockToAbsVertical(targetAbsColIdx), block);
                }
            }
        }
    }
}
