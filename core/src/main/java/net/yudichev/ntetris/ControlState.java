package net.yudichev.ntetris;

import java.util.function.Consumer;

public interface ControlState {
    void forAllPressedKeys(double gameTimeMillis, Consumer<GameControl> activeControlConsumer);
}
