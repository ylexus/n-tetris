package net.yudichev.ntetris;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

final class KeyPressTracker implements KeyState {
    private static final Logger logger = LoggerFactory.getLogger(KeyPressTracker.class);
    private static final int KEY_PROCESSING_PERIOD = 100;
    private static final Long ZERO = 0L;

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);
    // TODO abstract these out and name them as drop/rotate keys
    private final Set<KeyCode> unrepeatableKeyCodes = EnumSet.of(KeyCode.K, KeyCode.D, KeyCode.A, KeyCode.SEMICOLON, KeyCode.BACK_SPACE);
    private final List<KeyCode> queue = new ArrayList<>(64);
    private final Map<KeyCode, Long> timeKeyLastProcessedByKeyCode = new EnumMap<>(KeyCode.class);

    KeyPressTracker(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (pressedKeys.add(event.getCode())) {
                logger.info("pressed {}", event.getCode());
                queue.add(event.getCode());
            }
            event.consume();
        });
        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            event.consume();
        });
    }

    @Override
    public void forAllPressedKeys(long gameTimeMillis, Consumer<KeyCode> pressedKeyConsumer) {
        queue.forEach(keyCode -> {
            pressedKeyConsumer.accept(keyCode);
            timeKeyLastProcessedByKeyCode.put(keyCode, gameTimeMillis);
        });

        pressedKeys.forEach(keyCode -> {
            if (!unrepeatableKeyCodes.contains(keyCode) && !queue.contains(keyCode)) {
                long timeKeysLastProcessed = timeKeyLastProcessedByKeyCode.getOrDefault(keyCode, ZERO);
                if (gameTimeMillis - timeKeysLastProcessed > KEY_PROCESSING_PERIOD) {
                    logger.info("repeat {}", keyCode);
                    pressedKeyConsumer.accept(keyCode);
                    timeKeyLastProcessedByKeyCode.put(keyCode, gameTimeMillis);
                }
            }
        });

        queue.clear();
    }
}
