package net.yudichev.ntetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static net.yudichev.ntetris.GameControl.*;

final class GdxControlState implements ControlState {

    private static final Logger logger = LoggerFactory.getLogger(GdxControlState.class);
    private static final int KEY_PROCESSING_PERIOD = 100;
    private static final Long ZERO = 0L;

    private final Set<GameControl> pressedKeys = EnumSet.noneOf(GameControl.class);
    private final Set<GameControl> unrepeatableGameControls = EnumSet.of(LEFT_PLAYER_DROP, RIGHT_PLAYER_DROP, LEFT_PLAYER_ROTATE, RIGHT_PLAYER_ROTATE, PAUSE);
    private final List<GameControl> queue = new ArrayList<>(64);
    private final Map<GameControl, Long> timeKeyLastProcessedByGameControl = new EnumMap<>(GameControl.class);

    GdxControlState() {
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keyCode) {
                GameControl gameControl = gdxKeyCodeToControl(keyCode);
                if (gameControl != null && pressedKeys.add(gameControl)) {
                    logger.info("pressed {}", gameControl);
                    queue.add(gameControl);
                }
                return true;
            }

            @Override
            public boolean keyUp(int keyCode) {
                pressedKeys.remove(gdxKeyCodeToControl(keyCode));
                return true;
            }

            @Override
            public boolean keyTyped(char character) {
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return true;
            }
        });
    }

    @Override
    public void forAllPressedKeys(long gameTimeMillis, Consumer<GameControl> activeControlConsumer) {
        queue.forEach(gameControl -> {
            activeControlConsumer.accept(gameControl);
            timeKeyLastProcessedByGameControl.put(gameControl, gameTimeMillis);
        });

        pressedKeys.forEach(keyCode -> {
            if (!unrepeatableGameControls.contains(keyCode) && !queue.contains(keyCode)) {
                long timeKeysLastProcessed = timeKeyLastProcessedByGameControl.getOrDefault(keyCode, ZERO);
                if (gameTimeMillis - timeKeysLastProcessed > KEY_PROCESSING_PERIOD) {
                    logger.info("repeat {}", keyCode);
                    activeControlConsumer.accept(keyCode);
                    timeKeyLastProcessedByGameControl.put(keyCode, gameTimeMillis);
                }
            }
        });

        queue.clear();
    }
}
