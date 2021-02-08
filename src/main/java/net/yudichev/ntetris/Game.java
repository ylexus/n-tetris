package net.yudichev.ntetris;

import javafx.scene.input.KeyEvent;
import net.yudichev.ntetris.canvas.GameCanvas;

import java.util.List;

public interface Game {
    void render(double gameTimeMillis, long frameNumber, GameCanvas canvas, List<KeyEvent> keyEventQueue);
}
