package net.yudichev.ntetris.canvas;

import net.yudichev.ntetris.game.Player;

public interface GameCanvas {
    void renderRubble(double x, double y);

    void renderBlock(double x, double y, Player player);
}
