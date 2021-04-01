package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class EffectScene {
    private static final Logger logger = LoggerFactory.getLogger(EffectScene.class);

    // leftmost row first, column indexes from top to bottom
    private final CollapsingRubbleBlock[][] collapsingRubbleRows;
    private final GameCanvas canvas;

    EffectScene(int width, int height, GameCanvas canvas) {
        this.canvas = checkNotNull(canvas);
        collapsingRubbleRows = new CollapsingRubbleBlock[width][height];
    }

    public void collapseRubble(int colIdx, double gameTime) {
        logger.debug("Col {} collapsed", colIdx);
        for (int rowIdx = 0; rowIdx < collapsingRubbleRows[colIdx].length; rowIdx++) {
            collapsingRubbleRows[colIdx][rowIdx] = new CollapsingRubbleBlock(canvas, colIdx, rowIdx, gameTime);
        }
    }

    public void render(double gameTime) {
        for (int colIdx = 0; colIdx < collapsingRubbleRows.length; colIdx++) {
            for (int rowIdx = 0; rowIdx < collapsingRubbleRows[colIdx].length; rowIdx++) {
                CollapsingRubbleBlock collapsingRubbleBlock = collapsingRubbleRows[colIdx][rowIdx];
                if (collapsingRubbleBlock != null) {
                    collapsingRubbleBlock.onFrameStart(gameTime);
                    if (!collapsingRubbleBlock.calculate()) {
                        collapsingRubbleRows[colIdx][rowIdx] = null;
                    }
                    collapsingRubbleBlock.render(canvas);
                }
            }
        }
    }
}
