package net.yudichev.ntetris.canvas;

public interface GameCanvas {
    void renderBlock(double x, double y, Block block);

    void renderText(String text);
}
