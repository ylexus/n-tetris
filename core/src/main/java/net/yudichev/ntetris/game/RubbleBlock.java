package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.game.Sprite;

import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_RUBBLE;
import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class RubbleBlock extends GameBlock<RubbleShape> {
    private RubbleShape shape;

    RubbleBlock(GameScene gameScene, GameCanvas canvas, RubbleShape shape) {
        super(gameScene, canvas, Block.of(Sprite.RUBBLE_NORMAL));
        this.shape = checkNotNull(shape);
    }

    @Override
    public void render() {
        renderShape(shape);
    }

    public RubbleBlock transitionTo(RubbleShape newShape) {
        logger.debug("rubble {}: transition to {}", shape, newShape);
        shape = newShape;
        return this;
    }

    public void move() {
        @SuppressWarnings("NumericCastThatLosesPrecision") // exactly what's intended
        long outstandingDropSteps = (long) (timeSinceLastMove / DROP_STEP_DURATION_RUBBLE);
        logger.debug("{}: rubble {}: timeSinceLastMove {}", gameTime, shape, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTime;
            sourceShapeWhenTransitioning = shape;
            do {
                logger.debug("{}: rubble {}: outstanding steps {}", gameTime, shape, outstandingDropSteps);
                gameScene.moveRubble(shape);
            } while (--outstandingDropSteps > 0);
            timeSinceLastMove = 0;
        } else {
            // stop transition if needed
            if (timeSinceLastMove >= DROP_TRANSITION_STEP_DURATION) {
                sourceShapeWhenTransitioning = null;
            }
        }
    }
}
