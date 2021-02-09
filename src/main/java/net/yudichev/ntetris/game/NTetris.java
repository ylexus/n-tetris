package net.yudichev.ntetris.game;

import javafx.scene.input.KeyEvent;
import net.yudichev.ntetris.Game;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import static net.yudichev.ntetris.game.GameConstants.DROP_TRANSITION_STEP_DURATION;

// all time units are millis
// all coordinates are relative (0...1) except when mentioned
public final class NTetris implements Game {
    private static final Logger logger = LoggerFactory.getLogger(NTetris.class);

    private static final Shape[] ALL_SHAPES = Shape.values();
    private final Random random = new Random();

    private final Scene scene;
    private final int sceneWidthBlocks;
    private final int sceneHeightBlocks;
    private final Map<Player, PlayerState> stateByPlayer = new EnumMap<>(Player.class);
    private boolean gameOver;

    public NTetris(Settings settings) {
        sceneWidthBlocks = settings.playerZoneWidthInBlocks() * 2 + 1;
        sceneHeightBlocks = settings.playerZoneHeightInBlocks();
        scene = new Scene(sceneWidthBlocks, sceneHeightBlocks);
//        scene.addRubble(sceneWidthBlocks / 2, fullRow(sceneHeightBlocks));
        for (Player player : Player.ALL_PLAYERS) {
            stateByPlayer.put(player, new PlayerState(player, scene));
        }
    }

    @Override
    public void render(long gameTimeMillis, GameCanvas canvas, List<KeyEvent> keyEventQueue) {
        forEachPlayer(playerState -> playerState.onFrameStart(gameTimeMillis));

        if (!gameOver) {
            processVertMovements(keyEventQueue);
            forEachPlayer(PlayerState::lowerShape);
        }

        boolean shouldContinueGame = true;

        // render shapes
        for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
            Player player = Player.ALL_PLAYERS[i];
            var playerState = stateByPlayer.get(player);
            var destinationDrop = scene.getShapeDropsByPlayer().get(player);
            if (destinationDrop != null) {
                var sourceDropWhenTransitioning = playerState.getSourceDropWhenTransitioning();
                var transitionProportion = (double) playerState.getTimeSinceLastDrop() / DROP_TRANSITION_STEP_DURATION;
                logger.debug("render {} src {} dest {}, proportion {}", player, sourceDropWhenTransitioning, destinationDrop, transitionProportion);
                renderShape(canvas, player, sourceDropWhenTransitioning, destinationDrop, transitionProportion);
            } else if (!gameOver) {
                // player has no shape - spawn them one (if not on penalty)!
                if (playerState.shouldSpawnNewShape()) {
                    shouldContinueGame = attemptToSpawnNewShape(player);
                }

                playerState.stopTransitioning();
            }
        }

        renderRubble(canvas);

        if (!shouldContinueGame) {
            logger.info("GAME OVER");
            scene.deletePlayerShapes();
            gameOver = true;
        }
        if (gameOver) {
            canvas.renderGameOver();
        }
    }

    void forEachPlayer(Consumer<PlayerState> action) {
        for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
            action.accept(stateByPlayer.get(Player.ALL_PLAYERS[i]));
        }
    }

    private void processVertMovements(List<KeyEvent> keyEventQueue) {
        for (KeyEvent keyEvent : keyEventQueue) {
            switch (keyEvent.getCode()) {
                case W -> scene.movePlayerShapeVertically(Player.LEFT, -1);
                case S -> scene.movePlayerShapeVertically(Player.LEFT, 1);
                case O -> scene.movePlayerShapeVertically(Player.RIGHT, -1);
                case L -> scene.movePlayerShapeVertically(Player.RIGHT, 1);
                case D -> stateByPlayer.get(Player.LEFT).dropShape();
                case K -> stateByPlayer.get(Player.RIGHT).dropShape();
            }
            keyEvent.consume();
        }
    }

    private boolean attemptToSpawnNewShape(Player player) {
        var shape = ALL_SHAPES[random.nextInt(ALL_SHAPES.length)];
        int horizontalOffset;
        int horizontalSpeed;
        switch (player) {
            case LEFT -> {
                horizontalOffset = 0;
                horizontalSpeed = 1;
            }
            case RIGHT -> {
                horizontalOffset = sceneWidthBlocks - shape.getPattern().height();
                horizontalSpeed = -1;
            }
            default -> throw new IllegalArgumentException();
        }

        var verticalOffsetRange = sceneHeightBlocks - shape.getPattern().width();

        return findSpawnPointAndSpawn(
                verticalOffsetRange,
                verticalOffset -> scene
                        .attemptAddPlayerShape(player, ShapeDrop.of(shape.getPattern(), horizontalOffset, verticalOffset, horizontalSpeed)));
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
        var candidateFreeIdx = random.nextInt(numberOfEmptySlots);
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

    private int emptySlots(boolean[] slots) {
        int result = 0;
        for (boolean slot : slots) {
            if (!slot) {
                result++;
            }
        }
        return result;
    }

    private void renderShape(GameCanvas canvas,
                             Player player,
                             @Nullable ShapeDrop sourceDropWhenTransitioning,
                             ShapeDrop destinationDrop,
                             double transitionProportion) {
        for (int rowIdx = 0; rowIdx < destinationDrop.shape().getRows().size(); rowIdx++) {
            Row row = destinationDrop.shape().getRows().get(rowIdx);
            for (int colIdx = 0; colIdx < row.getElements().length; colIdx++) {
                if (row.getElements()[colIdx]) {
                    double targetAbsRowIdx = rowIdx + destinationDrop.horizontalOffset();
                    double targetAbsColIdx = colIdx + destinationDrop.verticalOffset();
                    if (sourceDropWhenTransitioning != null) {
                        // extrapolate transition
                        var sourceAbsRowIdx = rowIdx + sourceDropWhenTransitioning.horizontalOffset();
                        var sourceAbsColIdx = colIdx + sourceDropWhenTransitioning.verticalOffset();
                        logger.debug("render {} trans from {}, {} -> {}, {}", player, sourceAbsRowIdx, sourceAbsColIdx, targetAbsRowIdx, targetAbsColIdx);
                        targetAbsRowIdx = sourceAbsRowIdx + (targetAbsRowIdx - sourceAbsRowIdx) * transitionProportion;
                        targetAbsColIdx = sourceAbsColIdx + (targetAbsColIdx - sourceAbsColIdx) * transitionProportion;
                        logger.debug("render {} trans to {}, {}", player, targetAbsRowIdx, targetAbsColIdx);
                    }
                    logger.debug("render {} block {},{} at {}, {}", player, rowIdx, colIdx, targetAbsRowIdx, targetAbsColIdx);
                    canvas.renderBlock(blockToAbsHorizontal(targetAbsRowIdx), blockToAbsVertical(targetAbsColIdx), player);
                }
            }
        }
    }

    private void renderRubble(GameCanvas canvas) {
        for (int rowIdx = 0; rowIdx < scene.getRubble().size(); rowIdx++) {
            Row row = scene.getRubble().get(rowIdx);
            var rowElements = row.getElements();
            for (int colIdx = 0; colIdx < rowElements.length; colIdx++) {
                boolean element = rowElements[colIdx];
                if (element) {
                    canvas.renderRubble(blockToAbsHorizontal(rowIdx), blockToAbsVertical(colIdx));
                }
            }
        }
    }

    private double blockToAbsHorizontal(double blocksRelativeToLeft) {
        return (blocksRelativeToLeft + 0.5) / sceneWidthBlocks;
    }

    private double blockToAbsVertical(double blocksRelativeToTop) {
        return (blocksRelativeToTop + 0.5) / sceneHeightBlocks;
    }
}
