package net.yudichev.ntetris;

import java.util.function.Consumer;

public interface ControlState {
    void forAllActiveControls(double gameTime, Consumer<GameControl> activeControlConsumer);
}
