package net.yudichev.ntetris.game;

import net.yudichev.ntetris.ControlState;
import net.yudichev.ntetris.Game;
import net.yudichev.ntetris.GameControl;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.sound.Sounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static net.yudichev.ntetris.sound.Sounds.Sample.RUBBLE_COLLAPSE;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

// all time units are millis
// all coordinates are relative (0...1) except when mentioned
public final class NTetris implements Game {
    private static final Logger logger = LoggerFactory.getLogger(NTetris.class);

    private final Map<Player, PlayerBlock> shapeByPlayer = new EnumMap<>(Player.class);
    private final Map<RubbleShape, RubbleBlock> rubbleBlocksByShape;
    private final ArrayList<RubbleBlock> rubbleBlockBuffer;
    private final GameCanvas canvas;
    private final Sounds sounds;
    private final ControlState controlState;
    private final GameScene gameScene;
    private boolean gameOver;

    private boolean paused;
    private double lastPausedTime = Double.MIN_VALUE;
    private double totalPausedTimeSpan;

    public NTetris(Settings settings, GameCanvas canvas, Sounds sounds, ControlState controlState) {
        this.canvas = checkNotNull(canvas);
        this.sounds = checkNotNull(sounds);
        this.controlState = checkNotNull(controlState);
        int sceneWidthBlocks = settings.playerZoneWidthInBlocks() * 2 + 1;
        int sceneHeightBlocks = settings.playerZoneHeightInBlocks();
        rubbleBlocksByShape = new HashMap<>(sceneHeightBlocks * sceneHeightBlocks);
        gameScene = new GameScene(
                sceneWidthBlocks,
                sceneHeightBlocks,
                new RubbleLifecycleListener() {
                    @Override
                    public void onRubbleAdded(RubbleShape rubbleShape) {
                        logger.debug("New rubble {}", rubbleShape);
                        rubbleBlocksByShape.put(rubbleShape, new RubbleBlock(gameScene, NTetris.this.canvas, rubbleShape));
                    }

                    @Override
                    public void onRubbleRemoved(RubbleShape rubbleShape) {
                        logger.debug("Rubble collapsed {}", rubbleShape);
                        NTetris.this.sounds.play(RUBBLE_COLLAPSE);
                        rubbleBlocksByShape.remove(rubbleShape);
                    }

                    @Override
                    public void onRubbleAmended(RubbleShape oldRubbleShape, RubbleShape newRubbleShape) {
                        RubbleBlock rubbleBlock = rubbleBlocksByShape.remove(oldRubbleShape);
                        rubbleBlock.transitionTo(newRubbleShape);
                        rubbleBlocksByShape.put(newRubbleShape, rubbleBlock);
                    }

                    @Override
                    public void onRubbleColumnCollapsed(int colIdx) {
                        // TODO hook explosion effect
                    }
                });
        //        gameScene.addRubble(sceneWidthBlocks / 2, fullRow(sceneHeightBlocks));
        for (Player player : Player.ALL_PLAYERS) {
            shapeByPlayer.put(player, new PlayerBlock(player, gameScene, canvas));
        }

        gameScene.addRubbleColumnWithHole(gameScene.getWidth() / 2, 1);
        gameScene.addRubbleColumnWithHole(gameScene.getWidth() / 2 + 1, gameScene.getHeight() - 2);

        rubbleBlockBuffer = new ArrayList<>(sceneHeightBlocks * sceneWidthBlocks);
    }

    @Override
    public void render(double gameTime) {
        canvas.beginFrame();

        double gameTimeMillis = offsetGameTime(gameTime);

        forEachPlayer(playerShape -> playerShape.onFrameStart(gameTimeMillis));
        rubbleBlocksByShape.values().forEach(rubbleBlock -> rubbleBlock.onFrameStart(gameTimeMillis));

        if (!gameOver) {
            processKeys(gameTimeMillis);

            if (!paused) {
                rubbleBlockBuffer.clear();
                rubbleBlockBuffer.addAll(rubbleBlocksByShape.values());
                rubbleBlockBuffer.forEach(RubbleBlock::move);

                for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
                    gameOver = !shapeByPlayer.get(Player.ALL_PLAYERS[i]).lower();
                    if (gameOver) {
                        break;
                    }
                }
            }
        }
        if (gameOver) {
            forEachPlayer(PlayerBlock::gameOver);
        }

        forEachPlayer(PlayerBlock::render);
        rubbleBlocksByShape.values().forEach(RubbleBlock::render);

        if (paused) {
            canvas.renderText("Paused");
        }
        if (gameOver) {
            canvas.renderText("Game Over");
        }

        canvas.endFrame();
    }

    @SuppressWarnings("FloatingPointEquality")
    private double offsetGameTime(double gameTimeMillis) {
        if (paused) {
            if (lastPausedTime == Double.MIN_VALUE) {
                lastPausedTime = gameTimeMillis;
                logger.debug("Paused at {}", lastPausedTime);
            }
            gameTimeMillis = lastPausedTime;
        } else {
            if (lastPausedTime != Double.MIN_VALUE) {
                totalPausedTimeSpan += gameTimeMillis - lastPausedTime;
                logger.debug("Un-paused at {}, last paused at {}, total time on pause {}", gameTimeMillis, lastPausedTime, totalPausedTimeSpan);
                lastPausedTime = Double.MIN_VALUE;
            }
        }
        logger.debug("Real time {}, total paused {}, game time {}", gameTimeMillis, totalPausedTimeSpan, gameTimeMillis - totalPausedTimeSpan);
        gameTimeMillis -= totalPausedTimeSpan;
        return gameTimeMillis;
    }

    private void forEachPlayer(Consumer<PlayerBlock> action) {
        for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
            action.accept(shapeByPlayer.get(Player.ALL_PLAYERS[i]));
        }
    }

    private void processKeys(double gameTimeMillis) {
        controlState.forAllPressedKeys(gameTimeMillis, gameControl -> {
            if (gameControl == GameControl.PAUSE) {
                paused = !paused;
            }
            if (!paused) {
                switch (gameControl) {
                    case LEFT_PLAYER_UP:
                        gameScene.movePlayerShapeVertically(Player.LEFT, -1);
                        break;
                    case LEFT_PLAYER_DOWN:
                        gameScene.movePlayerShapeVertically(Player.LEFT, 1);
                        break;
                    case RIGHT_PLAYER_UP:
                        gameScene.movePlayerShapeVertically(Player.RIGHT, -1);
                        break;
                    case RIGHT_PLAYER_DOWN:
                        gameScene.movePlayerShapeVertically(Player.RIGHT, 1);
                        break;
                    case LEFT_PLAYER_ROTATE:
                        gameScene.rotatePlayersShape(Player.LEFT);
                        break;
                    case RIGHT_PLAYER_ROTATE:
                        gameScene.rotatePlayersShape(Player.RIGHT);
                        break;
                    case LEFT_PLAYER_DROP:
                        shapeByPlayer.get(Player.LEFT).drop();
                        break;
                    case RIGHT_PLAYER_DROP:
                        shapeByPlayer.get(Player.RIGHT).drop();
                        break;
                }
            }
        });
    }
}
