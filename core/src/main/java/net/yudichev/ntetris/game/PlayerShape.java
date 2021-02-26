package net.yudichev.ntetris.game;

import com.badlogic.gdx.graphics.Color;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;

import java.util.Random;
import java.util.function.IntFunction;

import static net.yudichev.ntetris.game.GameConstants.*;
import static net.yudichev.ntetris.game.Scene.ShapeLoweringResult;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class PlayerShape extends GameShape {

    private static final PlayerShapeType[] ALL_SHAPE_TYPES = PlayerShapeType.values();

    private final Random random = new Random();
    /**
     * -1 means no deadline
     */
    private final Player player;
    private long penaltyDeadline = -1;
    private boolean gameOver;

    PlayerShape(Player player, Scene scene, GameCanvas canvas) {
        super(scene, canvas, Block.of(player == Player.LEFT ? Color.RED : Color.GREEN, player.name()));
        this.player = checkNotNull(player);
    }

    public boolean lower() {
        long outstandingDropSteps = timeSinceLastMove / DROP_STEP_DURATION_PLAYER;
        logger.debug("{}: {}:  timeSinceLastMove {}", gameTimeMillis, player, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTimeMillis;
            sourceShapeWhenTransitioning = scene.getPlayerShapesByPlayer().get(player);
            do {
                logger.debug("{}: {}: outstanding steps {}", gameTimeMillis, player, outstandingDropSteps);
                ShapeLoweringResult loweringResult = scene.lowerShape(player);
                logger.debug("player lowering results {}", loweringResult);
                if (loweringResult != null) {
                    processLoweringResult(loweringResult);
                }
            } while (--outstandingDropSteps > 0);
            timeSinceLastMove = 0;
        } else {
            // stop transition if needed
            if (timeSinceLastMove >= DROP_TRANSITION_STEP_DURATION) {
                stopTransitioning();
            }
        }

        boolean shouldContinueGame = true;
        if (!scene.getPlayerShapesByPlayer().containsKey(player) && !gameOver) {
            // player has no shape - spawn one (if not on penalty)!
            if (shouldSpawnNewShape()) {
                shouldContinueGame = attemptToSpawnNewShape(player);
            }

            stopTransitioning();
        }
        gameOver = !shouldContinueGame;
        if (gameOver) {
            scene.deletePlayerShape(player);
        }
        return shouldContinueGame;
    }

    public void dropShape() {
        ShapeLoweringResult shapeLoweringResult = scene.dropShape(player);
        if (shapeLoweringResult != null) {
            processLoweringResult(shapeLoweringResult);
        }
    }

    @Override
    public void render() {
        Shape destinationShape = scene.getPlayerShapesByPlayer().get(player);
        if (destinationShape != null) {
            renderShape(destinationShape);
        }
    }

    public void gameOver() {
        gameOver = true;
        scene.deletePlayerShape(player);
    }

    private void stopTransitioning() {
        sourceShapeWhenTransitioning = null;
    }

    private boolean shouldSpawnNewShape() {
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

    private boolean attemptToSpawnNewShape(Player player) {
        PlayerShapeType shape = ALL_SHAPE_TYPES[random.nextInt(ALL_SHAPE_TYPES.length)];
        int horizontalOffset;
        int horizontalSpeed;
        switch (player) {
            case LEFT:
                horizontalOffset = 0;
                horizontalSpeed = 1;
                break;
            case RIGHT:
                horizontalOffset = scene.getWidth() - shape.getPattern().height();
                horizontalSpeed = -1;
                break;
            default:
                throw new IllegalArgumentException("Unsupported player " + player);
        }

        int verticalOffsetRange = scene.getHeight() - shape.getPattern().width();

        return findSpawnPointAndSpawn(
                verticalOffsetRange,
                verticalOffset -> scene.attemptAddPlayerShape(player, Shape.of(shape.getPattern(),
                        horizontalOffset,
                        verticalOffset,
                        horizontalSpeed)));
    }

    private boolean findSpawnPointAndSpawn(int verticalOffsetRange, IntFunction<Boolean> spawner) {
        boolean[] attemptedDropVerticalOffsets = new boolean[verticalOffsetRange];
        int verticalOffset;
        int attempts = 0;
        boolean succeeded;
        do {
            verticalOffset = findRandomEmptySlot(attemptedDropVerticalOffsets);
            if (verticalOffset < 0) {
                return false;
            } else {
                attemptedDropVerticalOffsets[verticalOffset] = true;
            }
            succeeded = spawner.apply(verticalOffset);
        } while (!succeeded && ++attempts < verticalOffsetRange);
        return succeeded;
    }

    /**
     * @return slot or -1 if no empty slots available
     */
    private int findRandomEmptySlot(boolean[] slots) {
        int numberOfEmptySlots = emptySlots(slots);
        if (numberOfEmptySlots == 0) {
            return -1;
        }
        int candidateFreeIdx = random.nextInt(numberOfEmptySlots);
        int i;
        int j = 0;
        for (i = 0; i < slots.length; i++) {
            if (!slots[i] && j++ == candidateFreeIdx) {
                break;
            }
        }
        if (i == slots.length) {
            return -1;
        }
        return i;
    }

    private static int emptySlots(boolean[] slots) {
        int result = 0;
        for (boolean slot : slots) {
            if (!slot) {
                result++;
            }
        }
        return result;
    }


    private void processLoweringResult(ShapeLoweringResult shapeLoweringResult) {
        if (shapeLoweringResult == ShapeLoweringResult.REACHED_BOTTOM) {
            penaltyDeadline = gameTimeMillis + PLAYER_PENALTY_PAUSE;
            logger.info("Player {} reached bottom, penalty until {}", player, penaltyDeadline);
        }
    }
}
