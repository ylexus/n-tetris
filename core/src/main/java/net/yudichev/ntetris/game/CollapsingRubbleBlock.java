package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.Sprite;

final class CollapsingRubbleBlock extends GameBlock<EffectShape> {
    private static final double COLLAPSE_DURATION = 500;
    private final EffectShape shape;
    private double spawnTime = Double.MIN_VALUE;
    private double scale;

    CollapsingRubbleBlock(GameCanvas canvas, int colIdx, int rowIdx, double creationGameTime) {
        super(Sprite.RUBBLE_NORMAL, creationGameTime);
        shape = EffectShape.of(colIdx, rowIdx, 0);
    }

    /**
     * @return true if the block is still alive, false if it should disappear from the scene
     */
    public boolean calculate() {
        //noinspection FloatingPointEquality
        if (spawnTime == Double.MIN_VALUE) {
            spawnTime = gameTime;
        }
        scale = ((spawnTime + COLLAPSE_DURATION) - gameTime) / COLLAPSE_DURATION;
        return scale >= 0;
    }

    @Override
    public void render(GameCanvas canvas) {
        canvas.renderBlock(shape.offsetX(), shape.offsetY(), Sprite.RUBBLE_NORMAL, scale);
    }
}
