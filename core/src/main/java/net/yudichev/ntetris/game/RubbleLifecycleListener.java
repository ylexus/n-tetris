package net.yudichev.ntetris.game;

interface RubbleLifecycleListener {
    void onRubbleAdded(RubbleShape rubbleShape);

    void onRubbleRemoved(RubbleShape rubbleShape);

    void onRubbleAmended(RubbleShape oldRubbleShape, RubbleShape newRubbleShape);

    void onRubbleColumnCollapsed(int colIdx);
}
