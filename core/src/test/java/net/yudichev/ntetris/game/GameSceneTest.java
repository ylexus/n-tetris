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

        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
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
        assertThat(gameScene.getRubbleRows()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(1, 0, 0),
                        null,
                        null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 1, 0),
                        RubbleShape.of(1, 1, 0),
                        null,
                        null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 2, 0),
                        RubbleShape.of(1, 2, 0),
                        null,
                        null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 3, 0),
                        null,
                        null,
                        null)
        );
    }

    @Test
    void collapseScenario3_DoesDropUnrelatedRubble() {
        gameScene = new GameScene(4, 4, rubbleLifecycleListener);
        advanceTimeAndStartFrame(0.0);

        /*
         * _ R _ R
         * R R _ R
         * R R _ R
         * R _ _ _
         */
        gameScene.addRubbleColumnWithHole(0, 0);
        gameScene.addRubbleColumnWithHole(1, 3);
        gameScene.addRubbleColumnWithHole(3, 3);

        /*
         * _ R _ R
         * R R _ R
         * R R _ R
         * R B B _
         */
        gameScene.attemptAddPlayerShape(Player.RIGHT, PlayerShape.of(pattern(
                row(X, X)
        ), 1, 3, -1));

        moveRubble();

        advanceTimeAndStartFrame(DROP_STEP_DURATION_PLAYER);
        gameScene.dropShape(Player.RIGHT);

        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();

        /*
         * _ _ R _
         * R _ R _
         * R _ R _
         * R R _ _
         */
        assertThat(gameScene.getRubbleRows()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        RubbleShape.of(2, 0, 0),
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 1, 0),
                        null,
                        RubbleShape.of(2, 1, 0),
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 2, 0),
                        null,
                        RubbleShape.of(2, 2, 0),
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(0, 3, 0),
                        RubbleShape.of(1, 3, 0),
                        null,
                        null
                )
        );
    }

    @Test
    void collapseScenario() {
        gameScene = new GameScene(6, 6, rubbleLifecycleListener);
        advanceTimeAndStartFrame(0);

        /*
         * _ _ _ R R R
         * _ _ _ _ _ R
         * _ _ _ R R _
         * _ _ _ R R R
         * _ _ _ R R R
         * _ _ _ R R R
         */
        gameScene.addRubbleColumnWithHole(gameScene.getWidth() / 2, 1);
        gameScene.addRubbleColumnWithHole(gameScene.getWidth() / 2 + 1, 1);
        gameScene.addRubbleColumnWithHole(gameScene.getWidth() / 2 + 2, 2);
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

        assertThat(gameScene.getRubbleRows()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 0, 0),
                        RubbleShape.of(4, 0, 0),
                        RubbleShape.of(5, 0, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 1, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 2, 0),
                        RubbleShape.of(4, 2, 0),
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 3, 0),
                        RubbleShape.of(4, 3, 0),
                        RubbleShape.of(5, 3, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 4, 0),
                        RubbleShape.of(4, 4, 0),
                        RubbleShape.of(5, 4, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 5, 0),
                        RubbleShape.of(4, 5, 0),
                        RubbleShape.of(5, 5, 0)
                )
        );

        advanceTimeAndStartFrame(DROP_STEP_DURATION_PLAYER);
        gameScene.dropShape(Player.LEFT);

        /*
         * _ _ _ _ _ R
         * _ R R _ _ R
         * _ _ _ _ _ _
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         */
        assertThat(gameScene.getRubbleRows()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 0, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(1, 1, 1).withInvisibleWallX(4).withFallCausedBy(Player.LEFT),
                        RubbleShape.of(2, 1, 1).withInvisibleWallX(5).withFallCausedBy(Player.LEFT),
                        null,
                        null,
                        RubbleShape.of(5, 1, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 3, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 4, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 5, 0)
                )
        );

        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();
        advanceTimeAndStartFrame(DROP_STEP_DURATION_RUBBLE);
        moveRubble();

        /*
         * _ _ _ _ _ R
         * _ _ _ R R R
         * _ _ _ _ _ _
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         */

        assertThat(gameScene.getRubbleRows()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 0, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        RubbleShape.of(3, 1, 0),
                        RubbleShape.of(4, 1, 0),
                        RubbleShape.of(5, 1, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 3, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 4, 0)
                ),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        RubbleShape.of(5, 5, 0)
                )
        );
    }

    private void advanceTimeAndStartFrame(double timeIncrement) {
        gameTime += timeIncrement;
        gameScene.onFrameStart(gameTime);
    }

    private void moveRubble() {
        assertThat(gameScene.moveRubble()).isFalse();
    }
}