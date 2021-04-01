package net.yudichev.ntetris.game;

import net.yudichev.ntetris.ControlState;
import net.yudichev.ntetris.Game;
import net.yudichev.ntetris.GameControl;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.sound.Sounds;
import net.yudichev.ntetris.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.text.MessageFormat.format;
import static net.yudichev.ntetris.sound.Sounds.Sample.RUBBLE_COLLAPSE;
import static net.yudichev.ntetris.util.Preconditions.checkArgument;
import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

// all time units are millis
// all coordinates are relative (0...1) except when mentioned
public final class NTetris implements Game {
    private static final Logger logger = LoggerFactory.getLogger(NTetris.class);
    private final GameCanvas canvas;
    private final Sounds sounds;
    private final ControlState controlState;
    private final GameScene gameScene;
    private final EffectScene effectScene;
    private Map<Player, PlayerBlock> blockByPlayer;
    private boolean gameOver;
    private boolean paused;
    private double lastPausedTime = Double.MIN_VALUE;
    private double totalPausedTimeSpan;

    public NTetris(Settings settings, GameCanvas canvas, Sounds sounds, ControlState controlState) {
        this.canvas = checkNotNull(canvas);
        this.sounds = checkNotNull(sounds);
        this.controlState = checkNotNull(controlState);
        int sceneWidthBlocks = settings.sceneWidthBlocks();
        int sceneHeightBlocks = settings.sceneHeightBlocks();

        effectScene = new EffectScene(sceneWidthBlocks, sceneHeightBlocks, canvas);

        gameScene = new GameScene(
                sceneWidthBlocks,
                sceneHeightBlocks,
                (gameTime, colIdx) -> {
                    this.sounds.play(RUBBLE_COLLAPSE);
                    effectScene.collapseRubble(colIdx, gameTime);
                });
    }

    public void addRubbleColumnWithHole(int x, int holeIndex) {
        gameScene.addRubbleColumnWithHole(x, holeIndex);
    }

    @Override
    public void render(double realGameTime) {
        canvas.beginFrame();

        double gameTime = offsetGameTime(realGameTime);

        if (blockByPlayer == null) {
            blockByPlayer = new EnumMap<>(Player.class);
            for (Player player : Player.ALL_PLAYERS) {
                blockByPlayer.put(player, new PlayerBlock(player, gameScene, gameTime));
            }
        }

        forEachPlayer(playerBlock -> playerBlock.onFrameStart(gameTime));
        gameScene.onFrameStart(gameTime);

        if (!gameOver) {
            processKeys(gameTime);

            if (!paused) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: before move {}", gameTime, gameScene.prettyPrintRubble());
                }

                boolean failed = gameScene.moveRubble();
                if (failed) {
                    throw new IllegalStateException(format("{0}: Stuck resolving rubble fall, rubble:{1}", gameTime, gameScene.prettyPrintRubble()));
                }

                for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
                    PlayerBlock playerBlock = blockByPlayer.get(Player.ALL_PLAYERS[i]);
                    if (playerBlock != null) {
                        gameOver = !playerBlock.lower();
                        if (gameOver) {
                            break;
                        }
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("{}: after move {}", gameTime, gameScene.prettyPrintRubble());
                }
            }
        }
        if (gameOver) {
            forEachPlayer(PlayerBlock::gameOver);
        }

        forEachPlayer(playerBlock -> playerBlock.render(canvas));
        gameScene.render(canvas);
        effectScene.render(gameTime);

        if (paused) {
            canvas.renderText("Paused");
        }
        if (gameOver) {
            canvas.renderText("Game Over");
        }

        canvas.endFrame();
    }

    void initialiseFromPrettyPrint(double gameTime, String scenario, @Nullable PlayerShape leftPlayerShape, @Nullable PlayerShape rightPlayerShape) {
        gameScene.initialiseRubbleFromPrettyPrint(gameTime, scenario);
        blockByPlayer = new EnumMap<>(Player.class);
        if (leftPlayerShape != null) {
            checkArgument(gameScene.attemptAddPlayerShape(Player.LEFT, leftPlayerShape));
            blockByPlayer.put(Player.LEFT, new PlayerBlock(Player.LEFT, gameScene, gameTime));
        }
        if (rightPlayerShape != null) {
            checkArgument(gameScene.attemptAddPlayerShape(Player.RIGHT, rightPlayerShape));
            blockByPlayer.put(Player.RIGHT, new PlayerBlock(Player.RIGHT, gameScene, gameTime));
        }
    }

    String prettyPrintRubble() {
        return gameScene.prettyPrintRubble();
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
            PlayerBlock playerBlock = blockByPlayer.get(Player.ALL_PLAYERS[i]);
            if (playerBlock != null) {
                action.accept(playerBlock);
            }
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
                        blockByPlayer.get(Player.LEFT).drop();
                        break;
                    case RIGHT_PLAYER_DROP:
                        blockByPlayer.get(Player.RIGHT).drop();
                        break;
                }
            }
        });
    }
}
