package net.yudichev.ntetris.game;

import net.yudichev.ntetris.NoopGameJournal;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.sound.Sounds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_PLAYER;
import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_RUBBLE;
import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.RectangularPattern.singleBlock;
import static net.yudichev.ntetris.game.Row.row;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NTetrisTest {
    private final Random random = new Random(0);
    @Mock
    GameCanvas canvas;
    @Mock
    Sounds sounds;
    private NTetris tetris;
    private double gameTime;

    @Test
    void scenario1() {
        String scenario = "" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][   ][<11][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ][ O ][   ][   ][ O ][ O ][ O ][   ][   ][<11][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][<11][<11][<11][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][   ][<11][<11][<11][<11][<11][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][<11][<11][<11][<11][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][   ][<11][<11][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][   ][ O ][ O ][ O ][   ][<11][<11][<11][<11][   ][   ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][<11][<11][<11][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][ O ][   ][   ][ O ][ O ][ O ][ O ][ O ][   ][<11][<11][<11][   ][   ][   ][   ]\n" +
                "[   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][<11][<11][<11][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][   ][ O ][ O ][ O ][   ][ O ][ O ][<11][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][   ][ O ][ O ][   ][<11][<11][<11][   ][   ][   ][   ]\n";

        tetris = new NTetris(settingsForScenario(scenario),
                canvas,
                sounds,
                (gameTimeMillis, activeControlConsumer) -> {},
                new NoopGameJournal(),
                random::nextInt);
        tetris.initialiseFromPrettyPrint(gameTime, scenario, null, null);
        startGameTime();

        advanceGameTimeBy(DROP_STEP_DURATION_RUBBLE * 3);

        assertThat(tetris.prettyPrintRubble()).contains("" +
                //                                                       W
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ][ O ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][ O ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][   ][ O ][ O ][ O ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][   ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ]"
        );
    }

    @Test
    void scenario2() {
        String scenario = "" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ]";

        tetris = new NTetris(settingsForScenario(scenario),
                canvas,
                sounds,
                (gameTimeMillis, activeControlConsumer) -> {},
                new NoopGameJournal(),
                random::nextInt);
        tetris.initialiseFromPrettyPrint(gameTime, scenario,
                PlayerShape.of(pattern(row(true, true), row(true, true)), 1, 2, 1),
                PlayerShape.of(pattern(row(true, true, true), row(false, true, false)), 10, 0, -1));
        startGameTime();

        for (int i = 0; i < 200; i++) {
            advanceGameTimeBy(1000.0 / 60);
        }

        assertThat(tetris.prettyPrintRubble()).contains("\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][ O ][ O ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][ O ][ O ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][ O ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ][   ][   ][   ]"
        );
    }

    @Test
    void twoColumnCollapseScenario() {
        String rubble = "" +
                // 0    1    2    3    4    5    6    7    8    9   10   11
                "[   ][   ][   ][   ][ O ][ O ][ O ][   ][ O ][   ][   ][   ]\n" + // 0
                "[   ][   ][   ][   ][ O ][   ][ O ][ O ][   ][   ][   ][   ]\n" + // 1
                "[   ][   ][   ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" + // 2
                "[   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ][   ]\n" + // 3
                "[   ][   ][   ][   ][ O ][ O ][ O ][ O ][ O ][   ][   ][   ]\n" + // 4
                "[   ][   ][   ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n";  // 5

        tetris = new NTetris(settingsForScenario(rubble),
                canvas,
                sounds,
                (gameTimeMillis, activeControlConsumer) -> {},
                new NoopGameJournal(),
                random::nextInt);
        tetris.initialiseFromPrettyPrint(gameTime, rubble,
                PlayerShape.of(singleBlock(), 0, 0, 1),
                PlayerShape.of(pattern(row(true, true, true)), 4, 3, -1));

        startGameTime();

        // drop the player shape
        advanceGameTimeBy(DROP_STEP_DURATION_PLAYER);

        assertThat(tetris.prettyPrintRubble()).contains("" +
                // 0    1    2    3    4    5    6    7    8    9   10   11
                "[   ][   ][   ][   ][   ][<03][   ][   ][<05][   ][   ][   ]\n" + // 0
                "[   ][   ][   ][   ][   ][   ][   ][<04][   ][   ][   ][   ]\n" + // 1
                "[   ][   ][   ][   ][   ][<03][   ][   ][   ][   ][   ][   ]\n" + // 2
                "[   ][   ][   ][ O ][   ][<03][   ][   ][   ][   ][   ][   ]\n" + // 3
                "[   ][   ][   ][   ][   ][<03][   ][<04][<05][   ][   ][   ]\n" + // 4
                "[   ][   ][   ][   ][   ][<03][   ][   ][   ][   ][   ][   ]\n"   // 5
        );

        // should be 2 rubble steps (as two columns collapsed) + 1 step to stop the rubble
        advanceGameTimeBy(DROP_STEP_DURATION_RUBBLE * 3);
        assertThat(tetris.prettyPrintRubble()).contains("\n" +
                // 0    1    2    3    4    5    6    7    8    9   10   11
                "[   ][   ][   ][   ][ O ][   ][ O ][   ][   ][   ][   ][   ]\n" + // 0
                "[   ][   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ]\n" + // 1
                "[   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" + // 2
                "[   ][   ][   ][ O ][ O ][   ][   ][   ][   ][   ][   ][   ]\n" + // 3
                "[   ][   ][   ][   ][ O ][ O ][ O ][   ][   ][   ][   ][   ]\n" + // 4
                "[   ][   ][   ][   ][ O ][   ][   ][   ][   ][   ][   ][   ]\n"   // 5
        );
    }

    private void advanceGameTimeBy(double diff) {
        gameTime += diff;
        tetris.render(gameTime);
    }

    private void startGameTime() {
        gameTime = 0.0;
        tetris.render(gameTime);
    }

    private static Settings settingsForScenario(String scenario) {
        int width = scenario.substring(0, scenario.indexOf('\n')).length() / 5;
        return Settings.builder()
                .setSceneWidthBlocks(width)
                .setSceneHeightBlocks(scenario.length() / (width * 5 + 1))
                .build();
    }
}