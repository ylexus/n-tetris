package net.yudichev.ntetris.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.game.BlockLook;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@SuppressWarnings("NumericCastThatLosesPrecision") // GDX uses float arithmetic
public final class GdxGameCanvas implements GameCanvas {
    private static final int CAMERA_SIDE_LENGTH = 1_000_000;
    private final SpriteBatch batch = new SpriteBatch();
    //    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera;
    private final double blockWidth;
    private final double blockHeight;
    private final Map<BlockLook, Texture> blockTextureByLook;

    public GdxGameCanvas(Settings settings) {
//        img = new Texture("badlogic.jpg");
        camera = new OrthographicCamera();
        camera.setToOrtho(true, CAMERA_SIDE_LENGTH, CAMERA_SIDE_LENGTH);
        font.setColor(Color.BLACK);
        blockWidth = 1.0f / (settings.playerZoneWidthInBlocks() * 2);
        blockHeight = 1.0f / settings.playerZoneHeightInBlocks();
        blockTextureByLook = new EnumMap<>(Stream.of(BlockLook.values())
                .collect(toMap(Function.identity(), blockLook -> new Texture("BLOCK_" + blockLook.name() + ".png"))));
    }

    @Override
    public void beginFrame() {
        camera.update();
//        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void renderBlock(double x, double y, Block block) {
        double pixelX = x - blockWidth / 2;
        double pixelY = y - blockHeight / 2;

        batch.draw(blockTextureByLook.get(block.look()), toCamera(pixelX), toCamera(pixelY), toCamera(blockWidth), toCamera(blockHeight));
//        shapeRenderer.setColor(block.color());
//        shapeRenderer.rect();
    }

    @Override
    public void renderText(String text) {
        font.draw(batch, text, toCamera(0.5), toCamera(0.5));
    }

    @Override
    public void endFrame() {
//        shapeRenderer.end();
        batch.end();
    }

    private static float toCamera(double value) {
        return (float) (value * CAMERA_SIDE_LENGTH);
    }

    @Override
    public void close() {
        font.dispose();
//        shapeRenderer.dispose();
        batch.dispose();
        blockTextureByLook.values().forEach(Texture::dispose);
    }
}
