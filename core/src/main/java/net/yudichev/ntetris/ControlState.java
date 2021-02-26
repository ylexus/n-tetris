package net.yudichev.ntetris;

import java.util.function.Consumer;

public interface ControlState {
    void forAllPressedKeys(long gameTimeMillis, Consumer<GameControl> activeControlConsumer);
}
