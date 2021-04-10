package net.yudichev.ntetris.game;

import net.yudichev.ntetris.NoopGameJournal;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.canvas.Sprite;
import net.yudichev.ntetris.journal.JournalPlayer;
import net.yudichev.ntetris.sound.Sounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class JournalReplayingTest {
    private NTetris tetris;
    private GameCanvas canvas;
    private Sounds sounds;

    @BeforeEach
    void setUp() {
        canvas = new GameCanvas() {
            @Override
            public void beginFrame() {
            }

            @Override
            public void renderBlock(double blockX, double blockY, Sprite sprite, double scale) {
            }

            @Override
            public void renderText(String text) {
            }

            @Override
            public void endFrame() {
            }

            @Override
            public void close() {
            }
        };
        sounds = new Sounds() {
            @Override
            public void play(Sample sample) {
            }

            @Override
            public void close() {
            }
        };
    }

    @Test
    void hangingScenario1() {
        JournalPlayer player = new JournalPlayer("journal-hangingScenario1.txt", gameTime -> tetris.render(gameTime));
        Settings settings = player.settings();
        tetris = new NTetris(settings,
                canvas,
                sounds,
                player.controlState(),
                new NoopGameJournal(),
                player.randomNumberGenerator());
        tetris.addRubbleColumnWithHole(settings.sceneWidthBlocks() / 2, settings.sceneHeightBlocks() / 2);
        try {
            player.play();
        } finally {
            player.close();
        }
    }
}
