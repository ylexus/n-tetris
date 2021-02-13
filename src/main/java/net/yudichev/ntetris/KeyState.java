package net.yudichev.ntetris;

import javafx.scene.input.KeyCode;

import java.util.function.Consumer;

public interface KeyState {
    void forAllPressedKeys(long gameTimeMillis, Consumer<KeyCode> pressedKeyConsumer);
}
