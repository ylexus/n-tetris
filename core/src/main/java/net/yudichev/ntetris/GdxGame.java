package net.yudichev.ntetris;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import net.yudichev.ntetris.canvas.GdxGameCanvas;
import net.yudichev.ntetris.game.NTetris;
import net.yudichev.ntetris.sound.GdxSounds;

public class GdxGame extends ApplicationAdapter {
    private double gameTimeMillis = Float.MIN_VALUE;
    private Game game;
    private GdxGameCanvas canvas;
    private GdxSounds sounds;

    @Override
    public void create() {
        int height = 15;
        Settings settings = Settings.builder()
                .setSceneHeightBlocks(height)
                .setSceneWidthBlocks(height * 5 / 3)
                .build();

        canvas = new GdxGameCanvas(settings);
        sounds = new GdxSounds();
        NTetris tetris = new NTetris(settings, canvas, sounds, new GdxControlState());
        tetris.addRubbleColumnWithHole(settings.sceneWidthBlocks() / 2, settings.sceneHeightBlocks() / 2);
        game = tetris;
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
        sounds.close();
    }
}
