package net.yudichev.ntetris.game;

import javafx.scene.paint.Color;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;

import java.util.Random;
import java.util.function.IntFunction;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.ntetris.game.GameConstants.*;
import static net.yudichev.ntetris.game.Scene.ShapeLoweringResult;

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
        var outstandingDropSteps = timeSinceLastMove / DROP_STEP_DURATION;
        logger.debug("{}: {}:  timeSinceLastMove {}", gameTimeMillis, player, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTimeMillis;
            sourceShapeWhenTransitioning = scene.getPlayerShapesByPlayer().get(player);
            do {
                logger.debug("{}: {}: outstanding steps {}", gameTimeMillis, player, outstandingDropSteps);
                var loweringResult = scene.lowerShape(player);
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

        var shouldContinueGame = true;
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
        var shapeLoweringResult = scene.dropShape(player);
        if (shapeLoweringResult != null) {
            processLoweringResult(shapeLoweringResult);
        }
    }

    @Override
    public void render() {
        var destinationShape = scene.getPlayerShapesByPlayer().get(player);
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
        var shape = ALL_SHAPE_TYPES[random.nextInt(ALL_SHAPE_TYPES.length)];
        int horizontalOffset;
        int horizontalSpeed;
        switch (player) {
            case LEFT -> {
                horizontalOffset = 0;
                horizontalSpeed = 1;
            }
            case RIGHT -> {
                horizontalOffset = scene.getWidth() - shape.getPattern().height();
                horizontalSpeed = -1;
            }
            default -> throw new IllegalArgumentException("Unsupported player " + player);
        }

        var verticalOffsetRange = scene.getHeight() - shape.getPattern().width();

        return findSpawnPointAndSpawn(
                verticalOffsetRange,
                verticalOffset -> scene.attemptAddPlayerShape(player, Shape.of(shape.getPattern(),
                        horizontalOffset,
                        verticalOffset,
                        horizontalSpeed)));
    }

    private boolean findSpawnPointAndSpawn(int verticalOffsetRange, IntFunction<Boolean> spawner) {
        var attemptedDropVerticalOffsets = new boolean[verticalOffsetRange];
        int verticalOffset;
        var attempts = 0;
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
        var numberOfEmptySlots = emptySlots(slots);
        if (numberOfEmptySlots == 0) {
            return -1;
        }
        var candidateFreeIdx = random.nextInt(numberOfEmptySlots);
        int i;
        var j = 0;
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
        var result = 0;
        for (var slot : slots) {
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
