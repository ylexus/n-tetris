package net.yudichev.ntetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_PLAYER;
import static net.yudichev.ntetris.game.GameConstants.DROP_STEP_DURATION_RUBBLE;
import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GameSceneTest {
    @Mock
    RubbleLifecycleListener rubbleLifecycleListener;
    private GameScene gameScene;
    private double gameTime;

    @BeforeEach
    void setUp() {
    }

    @Test
    void collapseScenario2() {
        gameScene = new GameScene(4, 4, rubbleLifecycleListener);
        advanceTimeAndStartFrame(0);

        /*
         * _ R R R
         * R R R R
         * R R R R
         * R _ _ _
         */
        gameScene.addRubbleColumnWithHole(0, 0);
        gameScene.addRubbleColumnWithHole(1, 3);
        gameScene.addRubbleColumnWithHole(2, 3);
        gameScene.addRubbleColumnWithHole(3, 3);


        /*
         * _ R R R
         * R R R R
         * R R R R
         * R B B _
         */
        gameScene.attemptAddPlayerShape(Player.RIGHT, PlayerShape.of(pattern(
                row(X, X)
        ), 1, 3, -1));

        gameScene.dropShape(Player.RIGHT);

        // two steps as need to move two columns
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();

        /*
         * _ R _ _
         * R R _ _
         * R R _ _
         * R _ _ _
         */
        assertThat(gameScene.prettyPrintRubble()).contains("" +
                "[   ][ O ][   ][   ]\n" +
                "[ O ][ O ][   ][   ]\n" +
                "[ O ][ O ][   ][   ]\n" +
                "[ O ][   ][   ][   ]");
    }

    @Test
    void collapseScenario() {
        gameScene = new GameScene(6, 6, rubbleLifecycleListener);
        advanceTimeAndStartFrame(0);

        gameScene.initialiseRubbleFromPrettyPrint(gameTime, "" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]");
        gameScene.attemptAddPlayerShape(Player.LEFT, PlayerShape.of(PlayerShapeType.DASH.getPattern(), 0, 1, 1));
        // row drops as a drop just approaches
        gameScene.dropShape(Player.LEFT);

        /*
         * _ _ _ R R R
         * _ B B B B R
         * _ _ _ R R _
         * _ _ _ R R R
         * _ _ _ R R R
         * _ _ _ R R R
         */

        assertThat(gameScene.prettyPrintRubble()).contains("" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][   ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n");

        advanceTimeAndStartFrame(DROP_STEP_DURATION_PLAYER);
        gameScene.dropShape(Player.LEFT);

        assertThat(gameScene.prettyPrintRubble()).contains("" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][>04][>05][   ][   ][ O ]\n" +
                "[   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]");

        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();

        assertThat(gameScene.prettyPrintRubble()).contains("\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][ O ][ O ][ O ]\n" +
                "[   ][   ][   ][   ][   ][   ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]\n" +
                "[   ][   ][   ][   ][   ][ O ]");
    }

    @Test
    void collapseScenario3_DoesDropUnrelatedRubble() {
        gameScene = new GameScene(4, 4, rubbleLifecycleListener);
        gameScene.initialiseRubbleFromPrettyPrint(gameTime, "" +
                "[   ][ O ][   ][ O ]\n" +
                "[ O ][ O ][   ][ O ]\n" +
                "[ O ][ O ][   ][ O ]\n" +
                "[ O ][   ][   ][   ]");

        advanceTimeAndStartFrame(0.0);

        /*
         * _ R _ R
         * R R _ R
         * R R _ R
         * R B B _
         */
        gameScene.attemptAddPlayerShape(Player.RIGHT, PlayerShape.of(pattern(
                row(X, X)
        ), 1, 3, -1));

        gameScene.dropShape(Player.RIGHT);

        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();

        assertThat(gameScene.prettyPrintRubble()).contains("\n" +
                "[   ][   ][ O ][   ]\n" +
                "[ O ][   ][ O ][   ]\n" +
                "[ O ][   ][ O ][   ]\n" +
                "[ O ][ O ][   ][   ]");
    }

    private void advanceTimeAndStartFrame(double timeIncrement) {
        gameTime += timeIncrement;
        gameScene.onFrameStart(gameTime);
    }

    private void moveRubble() {
        assertThat(gameScene.moveRubble()).isFalse();
    }
}