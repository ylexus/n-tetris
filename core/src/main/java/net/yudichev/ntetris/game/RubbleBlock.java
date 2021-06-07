package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.Sprite;

import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;
import static net.yudichev.ntetris.game.GameConstants.INITIAL_DROP_STEP_DURATION_RUBBLE;
import static net.yudichev.ntetris.game.RubbleMoveResultFlags.isMoved;
import static net.yudichev.ntetris.game.RubbleMoveResultFlags.requiresMoreMoves;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class RubbleBlock extends GameBlock<RubbleShape> {
    private final GameScene gameScene;
    private RubbleShape shape;

    RubbleBlock(GameScene gameScene, RubbleShape shape, double creationGameTime) {
        super(Sprite.RUBBLE_NORMAL, creationGameTime);
        this.gameScene = checkNotNull(gameScene);
        this.shape = checkNotNull(shape);
    }

    @Override
    public void render(GameCanvas canvas) {
        renderShape(canvas, shape);
    }

    public RubbleBlock transitionTo(RubbleShape newShape) {
        logger.debug("{}: block {} shape {} transition to {}", gameTime, this, shape, newShape);
        shape = newShape;
        return this;
    }

    /**
     * @return see {@link GameScene#moveRubble(RubbleShape)}
     */
    public boolean move() {
        @SuppressWarnings("NumericCastThatLosesPrecision") // exactly what's intended
        long outstandingDropSteps = (long) (timeSinceLastMove / INITIAL_DROP_STEP_DURATION_RUBBLE);
        logger.debug("{}: rubble {}: timeSinceLastMove {}, lastMoveTime {}", gameTime, shape, timeSinceLastMove, lastMoveTime);
        boolean moved = false;
        boolean requiresMoreMoves;
        if (outstandingDropSteps > 0) {
            do {
                logger.debug("{}: rubble {}: outstanding steps {}", gameTime, shape, outstandingDropSteps);
                int result = gameScene.moveRubble(shape);
                requiresMoreMoves = requiresMoreMoves(result);
                moved |= isMoved(result);
            } while (!requiresMoreMoves && --outstandingDropSteps > 0);
            logger.debug("{}: rubble {}: moved={}, requiresMoreMoves={}", gameTime, shape, moved, requiresMoreMoves);
            if (!requiresMoreMoves) {
                lastMoveTime = gameTime;
                sourceShapeWhenTransitioning = shape;
                timeSinceLastMove = 0;
            }
        } else {
            // stop transition if needed
            if (timeSinceLastMove >= DROP_TRANSITION_STEP_DURATION) {
                sourceShapeWhenTransitioning = null;
            }
        }
        return moved;
    }

    RubbleShape getShape() {
        return shape;
    }
}
