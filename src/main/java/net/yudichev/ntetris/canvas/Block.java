package net.yudichev.ntetris.canvas;

import javafx.scene.paint.Color;

import static com.google.common.base.Preconditions.checkNotNull;

public class Block {
    private final Color color;
    private final String name;

    public Block(Color color, String name) {
        this.color = checkNotNull(color);
        this.name = checkNotNull(name);
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}