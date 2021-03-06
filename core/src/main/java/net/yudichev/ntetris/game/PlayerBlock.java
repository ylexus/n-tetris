package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;

import java.util.Random;
import java.util.function.IntFunction;

import static net.yudichev.ntetris.canvas.game.Sprite.LEFT_PLAYER_NORMAL;
import static net.yudichev.ntetris.canvas.game.Sprite.RIGHT_PLAYER_NORMAL;
import static net.yudichev.ntetris.game.GameConstants.*;
import static net.yudichev.ntetris.game.GameScene.ShapeLoweringResult;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class PlayerBlock extends GameBlock<PlayerShape> {

    private static final PlayerShapeType[] ALL_SHAPE_TYPES = PlayerShapeType.values();

    private final Random random = new Random();
    /**
     * -1 means no deadline
     */
    private final Player player;
    private double penaltyDeadline = Double.MIN_VALUE;
    private boolean gameOver;

    PlayerBlock(Player player, GameScene gameScene, GameCanvas canvas) {
        super(gameScene, canvas, Block.of(player == Player.LEFT ? LEFT_PLAYER_NORMAL : RIGHT_PLAYER_NORMAL));
        this.player = checkNotNull(player);
    }

    public boolean lower() {
        @SuppressWarnings("NumericCastThatLosesPrecision") // exactly what's intended - to get the floor
        long outstandingDropSteps = (long) (timeSinceLastMove / DROP_STEP_DURATION_PLAYER);
        logger.debug("{}: {}:  timeSinceLastMove {}", gameTime, player, timeSinceLastMove);
        if (outstandingDropSteps > 0) {
            lastMoveTime = gameTime;
            sourceShapeWhenTransitioning = gameScene.getPlayerShapesByPlayer().get(player);
            do {
                logger.debug("{}: {}: outstanding steps {}", gameTime, player, outstandingDropSteps);
                ShapeLoweringResult loweringResult = gameScene.lowerShape(player);
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
        if (!gameScene.getPlayerShapesByPlayer().containsKey(player) && !gameOver) {
            // player has no shape - spawn one (if not on penalty)!
            if (shouldSpawnNewShape()) {
                shouldContinueGame = attemptToSpawnNewShape(player);
            }

            stopTransitioning();
        }
        gameOver = !shouldContinueGame;
        if (gameOver) {
            gameScene.deletePlayerShape(player);
        }
        return shouldContinueGame;
    }

    public void drop() {
        ShapeLoweringResult shapeLoweringResult = gameScene.dropShape(player);
        if (shapeLoweringResult != null) {
            processLoweringResult(shapeLoweringResult);
        }
    }

    @Override
    public void render() {
        PlayerShape destinationShape = gameScene.getPlayerShapesByPlayer().get(player);
        if (destinationShape != null) {
            renderShape(destinationShape);
        }
    }

    public void gameOver() {
        gameOver = true;
        gameScene.deletePlayerShape(player);
    }

    private void stopTransitioning() {
        sourceShapeWhenTransitioning = null;
    }

    @SuppressWarnings("FloatingPointEquality")
    private boolean shouldSpawnNewShape() {
        logger.debug("{}: player {} has no shape, deadline {}", gameTime, player, penaltyDeadline);
        if (penaltyDeadline == Double.MIN_VALUE) {
            return true;
        }
        if (gameTime >= penaltyDeadline) {
            penaltyDeadline = Double.MIN_VALUE;
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
                horizontalOffset = gameScene.getWidth() - shape.getPattern().height();
                horizontalSpeed = -1;
                break;
            default:
                throw new IllegalArgumentException("Unsupported player " + player);
        }

        int verticalOffsetRange = gameScene.getHeight() - shape.getPattern().width();

        return findSpawnPointAndSpawn(
                verticalOffsetRange,
                verticalOffset -> gameScene.attemptAddPlayerShape(player, PlayerShape.of(shape.getPattern(),
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
            penaltyDeadline = gameTime + PLAYER_PENALTY_PAUSE;
            logger.debug("Player {} reached bottom, penalty until {}", player, penaltyDeadline);
        }
    }
}
