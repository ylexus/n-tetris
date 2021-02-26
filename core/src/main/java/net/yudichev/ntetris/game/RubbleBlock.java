package net.yudichev.ntetris.game;

import com.badlogic.gdx.graphics.Color;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;

import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_RUBBLE;
import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class RubbleBlock extends GameShape {
    private Shape shape;

    RubbleBlock(Scene scene, GameCanvas canvas, Shape shape) {
        super(scene, canvas, Block.of(Color.BLUE, "rubble").withShape(shape));
        this.shape = checkNotNull(shape);
    }

    @Override
    public void render() {
        renderShape(shape);
    }

    public void transitionTo(Shape newShape) {
        logger.debug("rubble {}: transition to {}", shape, newShape);
        shape = newShape;
    }

    public void move() {
        long outstandingDropSteps = timeSinceLastMove / DROP_STEP_DURATION_RUBBLE;
        logger.debug("{}: rubble {}: timeSinceLastMove {}", gameTimeMillis, shape, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTimeMillis;
            sourceShapeWhenTransitioning = shape;
            do {
                logger.debug("{}: rubble {}: outstanding steps {}", gameTimeMillis, shape, outstandingDropSteps);
                scene.moveRubble(shape);
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
