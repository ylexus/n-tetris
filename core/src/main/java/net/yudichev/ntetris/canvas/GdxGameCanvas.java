package net.yudichev.ntetris.canvas;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.yudichev.ntetris.Settings;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public final class GdxGameCanvas implements GameCanvas {
    //    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final OrthographicCamera camera;
    private final float blockWidth;
    private final float blockHeight;
//    private final Texture img;

    public GdxGameCanvas(Settings settings) {
//        img = new Texture("badlogic.jpg");
        camera = new OrthographicCamera();
        camera.setToOrtho(true, 1, 1);
        blockWidth = 1.0f / (settings.playerZoneWidthInBlocks() * 2);
        blockHeight = 1.0f / settings.playerZoneHeightInBlocks();
    }

    @Override
    public void renderBlock(double x, double y, Block block) {
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        float pixelX = (float) x - blockWidth / 2;
        float pixelY = (float) y - blockHeight / 2;

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(block.color());
        shapeRenderer.rect(pixelX, pixelY, blockWidth, blockHeight);
        shapeRenderer.end();
    }

    @Override
    public void renderText(String text) {

    }

    @Override
    public void close() {
        shapeRenderer.dispose();
//        img.dispose();
    }
}
