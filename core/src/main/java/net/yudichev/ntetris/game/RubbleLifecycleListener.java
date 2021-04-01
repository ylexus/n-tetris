package net.yudichev.ntetris.game;

interface RubbleLifecycleListener {
    void onRubbleColumnCollapsed(double gameTime, int colIdx);
}
