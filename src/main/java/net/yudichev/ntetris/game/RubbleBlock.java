package net.yudichev.ntetris.game;

import javafx.scene.paint.Color;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION;
import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;

final class RubbleBlock extends GameShape {
    private Shape shape;

    RubbleBlock(Scene scene, GameCanvas canvas, Shape shape) {
        super(scene, canvas, new Block(Color.BLUE, "rubble"));
        this.shape = checkNotNull(shape);
    }

    @Override
    public void render() {
        renderShape(shape);
    }

    public void transitionTo(Shape newShape) {
        logger.debug("rubble {}: transition to {}", shape, newShape);
        sourceShapeWhenTransitioning = shape;
        shape = newShape;
    }

    public void move() {
        var outstandingDropSteps = timeSinceLastMove / DROP_STEP_DURATION;
        logger.debug("{}: rubble {}: timeSinceLastMove {}", gameTimeMillis, shape, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTimeMillis;
            sourceShapeWhenTransitioning = shape;
            do {
                logger.debug("{}: rubble {}: outstanding steps {}", gameTimeMillis, shape, outstandingDropSteps);
                shape = scene.moveRubble(shape);
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
