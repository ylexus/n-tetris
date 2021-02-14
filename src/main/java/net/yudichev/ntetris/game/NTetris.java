package net.yudichev.ntetris.game;

import net.yudichev.ntetris.Game;
import net.yudichev.ntetris.KeyState;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

// all time units are millis
// all coordinates are relative (0...1) except when mentioned
public final class NTetris implements Game {
    private static final Logger logger = LoggerFactory.getLogger(NTetris.class);

    private final Map<Player, PlayerShape> shapeByPlayer = new EnumMap<>(Player.class);
    private final Map<Shape, RubbleBlock> rubbleBlocksByShape;
    private final ArrayList<RubbleBlock> rubbleBlockBuffer;
    private final GameCanvas canvas;
    private final KeyState keyState;
    private final Scene scene;
    private boolean gameOver;

    private boolean paused;
    private long lastPausedTime = -1;
    private long totalPausedTimeSpan;

    public NTetris(Settings settings, GameCanvas canvas, KeyState keyState) {
        this.canvas = checkNotNull(canvas);
        this.keyState = checkNotNull(keyState);
        var sceneWidthBlocks = settings.playerZoneWidthInBlocks() * 2 + 1;
        var sceneHeightBlocks = settings.playerZoneHeightInBlocks();
        rubbleBlocksByShape = new HashMap<>(sceneHeightBlocks * sceneHeightBlocks);
        scene = new Scene(sceneWidthBlocks,
                sceneHeightBlocks,
                this::onNewRubbleBlock,
                this::onRubbleBlockCollapsed,
                this::onRubbleAmended);
//        scene.addRubble(sceneWidthBlocks / 2, fullRow(sceneHeightBlocks));
        for (var player : Player.ALL_PLAYERS) {
            shapeByPlayer.put(player, new PlayerShape(player, scene, canvas));
        }

        scene.addRubbleColumnWithHole(scene.getWidth() / 2, 1);
        scene.addRubbleColumnWithHole(scene.getWidth() / 2 + 1, scene.getHeight() - 2);

        rubbleBlockBuffer = new ArrayList<>(sceneHeightBlocks * sceneWidthBlocks);
    }

    @Override
    public void render(long gameTime) {
        var gameTimeMillis = offsetGameTime(gameTime);

        forEachPlayer(playerShape -> playerShape.onFrameStart(gameTimeMillis));
        rubbleBlocksByShape.values().forEach(rubbleBlock -> rubbleBlock.onFrameStart(gameTimeMillis));

        if (!gameOver) {
            processKeys(gameTimeMillis);

            if (!paused) {
                rubbleBlockBuffer.clear();
                rubbleBlockBuffer.addAll(rubbleBlocksByShape.values());
                rubbleBlockBuffer.forEach(RubbleBlock::move);

                for (var i = 0; i < Player.ALL_PLAYERS.length; i++) {
                    gameOver = !shapeByPlayer.get(Player.ALL_PLAYERS[i]).lower();
                    if (gameOver) {
                        break;
                    }
                }
            }
        }
        if (gameOver) {
            forEachPlayer(PlayerShape::gameOver);
        }

        forEachPlayer(PlayerShape::render);
        rubbleBlocksByShape.values().forEach(RubbleBlock::render);

        if (paused) {
            canvas.renderText("Paused");
        }
        if (gameOver) {
            canvas.renderText("Game Over");
        }
    }

    private long offsetGameTime(long gameTimeMillis) {
        if (paused) {
            if (lastPausedTime == -1) {
                lastPausedTime = gameTimeMillis;
                logger.debug("Paused at {}", lastPausedTime);
            }
            gameTimeMillis = lastPausedTime;
        } else {
            if (lastPausedTime > 0) {
                totalPausedTimeSpan += gameTimeMillis - lastPausedTime;
                logger.debug("Un-paused at {}, last paused at {}, total time on pause {}", gameTimeMillis, lastPausedTime, totalPausedTimeSpan);
                lastPausedTime = -1;
            }
        }
        logger.debug("Real time {}, total paused {}, game time {}", gameTimeMillis, totalPausedTimeSpan, gameTimeMillis - totalPausedTimeSpan);
        gameTimeMillis -= totalPausedTimeSpan;
        return gameTimeMillis;
    }

    private void onNewRubbleBlock(Shape shape) {
        logger.debug("New rubble {}", shape);
        rubbleBlocksByShape.put(shape, new RubbleBlock(scene, canvas, shape));
    }

    private void onRubbleBlockCollapsed(Shape shape) {
        logger.debug("Rubble collapsed {}", shape);
        rubbleBlocksByShape.remove(shape);
    }

    private void onRubbleAmended(Shape oldRubbleBlock, Shape newRubbleBlock) {
        var rubbleBlock = rubbleBlocksByShape.remove(oldRubbleBlock);
        rubbleBlock.transitionTo(newRubbleBlock);
        rubbleBlocksByShape.put(newRubbleBlock, rubbleBlock);
    }

    private void forEachPlayer(Consumer<PlayerShape> action) {
        for (var i = 0; i < Player.ALL_PLAYERS.length; i++) {
            action.accept(shapeByPlayer.get(Player.ALL_PLAYERS[i]));
        }
    }

    private void processKeys(long gameTimeMillis) {
        keyState.forAllPressedKeys(gameTimeMillis, keyCode -> {
            switch (keyCode) {
                case W -> scene.movePlayerShapeVertically(Player.LEFT, -1);
                case S -> scene.movePlayerShapeVertically(Player.LEFT, 1);
                case O -> scene.movePlayerShapeVertically(Player.RIGHT, -1);
                case L -> scene.movePlayerShapeVertically(Player.RIGHT, 1);
                case A -> scene.rotatePlayersShape(Player.LEFT);
                case SEMICOLON -> scene.rotatePlayersShape(Player.RIGHT);
                case D -> shapeByPlayer.get(Player.LEFT).dropShape();
                case K -> shapeByPlayer.get(Player.RIGHT).dropShape();
                case BACK_SPACE -> paused = !paused;
            }
        });
    }
}
