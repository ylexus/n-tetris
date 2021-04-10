package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.Sprite;

import static net.yudichev.ntetris.util.Preconditions.checkNotNull;

final class StaticScene {
    private final int width;
    private final int height;
    private final GameCanvas canvas;

    StaticScene(int width, int height, GameCanvas canvas) {
        this.width = width;
        this.height = height;
        this.canvas = checkNotNull(canvas);
    }

    public void render(double gameTime) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                canvas.renderBlock(x, y, Sprite.GRID, 1.0);
            }
        }
    }
}
