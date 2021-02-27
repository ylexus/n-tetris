package net.yudichev.ntetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import net.yudichev.ntetris.canvas.GdxGameCanvas;
import net.yudichev.ntetris.game.NTetris;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdxGame extends ApplicationAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GdxGame.class);

    private double gameTimeMillis = Float.MIN_VALUE;
    private Game game;
    private GdxGameCanvas canvas;

    @Override
    public void create() {
        Settings settings = Settings.builder()
                .setPlayerZoneHeightInBlocks(30)
                .setPlayerZoneWidthInBlocks(30 / 3 * 5 / 2)
                .build();

        canvas = new GdxGameCanvas(settings);
        game = new NTetris(settings, canvas, new GdxControlState());
    }

    @Override
    public void render() {
        double deltaTime = Gdx.graphics.getDeltaTime();
        //noinspection FloatingPointEquality
        if (gameTimeMillis == Float.MIN_VALUE) {
            gameTimeMillis = 0;
        } else {
            gameTimeMillis += deltaTime * 1_000;
        }
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.render(gameTimeMillis);
    }

    @Override
    public void dispose() {
        canvas.close();
    }
}
