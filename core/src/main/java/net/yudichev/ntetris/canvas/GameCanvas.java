package net.yudichev.ntetris.canvas;

public interface GameCanvas {
    void beginFrame();

    void renderBlock(double blockX, double blockY, Sprite sprite, double scale);

    void renderText(String text);

    void endFrame();

    void close();
}
