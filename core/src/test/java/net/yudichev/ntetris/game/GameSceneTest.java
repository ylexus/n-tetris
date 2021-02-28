package net.yudichev.ntetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Objects;

import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.RectangularPattern.singleBlock;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GameSceneTest {
    @Mock
    RubbleLifecycleListener rubbleLifecycleListener;
    private GameScene gameScene;

    @BeforeEach
    void setUp() {
    }

    @Test
    void collapseScenario2() {
        gameScene = new GameScene(4, 4, rubbleLifecycleListener);

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
                row(X),
                row(X)
        ), 1, 3, -1));
        gameScene.dropShape(Player.RIGHT);

        moveRubble();
        moveRubble();
        moveRubble();
        moveRubble();
        moveRubble();

        /*
         * _ R _ _
         * R R _ _
         * R R _ _
         * R _ _ _
         */
        assertThat(gameScene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 0, 1, 0),
                        RubbleShape.of(singleBlock(), 0, 2, 0),
                        RubbleShape.of(singleBlock(), 0, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 1, 0, 0),
                        RubbleShape.of(singleBlock(), 1, 1, 0),
                        RubbleShape.of(singleBlock(), 1, 2, 0),
                        null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null)
        );
    }

    @Test
    void collapseScenario3_DoesNotDropUnrelatedRubble() {
        gameScene = new GameScene(4, 4, rubbleLifecycleListener);

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
                row(X),
                row(X)
        ), 1, 3, -1));
        gameScene.dropShape(Player.RIGHT);

        moveRubble();
        moveRubble();
        moveRubble();
        moveRubble();
        moveRubble();

        /*
         * _ _ _ R
         * R _ _ R
         * R _ _ R
         * R R _ _
         */
        assertThat(gameScene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 0, 1, 0),
                        RubbleShape.of(singleBlock(), 0, 2, 0),
                        RubbleShape.of(singleBlock(), 0, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null,
                        RubbleShape.of(singleBlock(), 1, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 3, 0, 0),
                        RubbleShape.of(singleBlock(), 3, 1, 0),
                        RubbleShape.of(singleBlock(), 3, 2, 0),
                        null)
        );
    }

    @Test
    void collapseScenario() {
        gameScene = new GameScene(6, 6, rubbleLifecycleListener);

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
        gameScene.attemptAddPlayerShape(Player.LEFT, PlayerShape.of(PlayerShapeType.I.getPattern(), 0, 1, 1));
        // tow drops as a drop just approaches
        gameScene.dropShape(Player.LEFT);

        /*
         * _ _ _ R R R
         * _ B B B B R
         * _ _ _ R R _
         * _ _ _ R R R
         * _ _ _ R R R
         * _ _ _ R R R
         */

        assertThat(gameScene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 3, 0, 0),
                        null,
                        RubbleShape.of(singleBlock(), 3, 2, 0),
                        RubbleShape.of(singleBlock(), 3, 3, 0),
                        RubbleShape.of(singleBlock(), 3, 4, 0),
                        RubbleShape.of(singleBlock(), 3, 5, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 4, 0, 0),
                        null,
                        RubbleShape.of(singleBlock(), 4, 2, 0),
                        RubbleShape.of(singleBlock(), 4, 3, 0),
                        RubbleShape.of(singleBlock(), 4, 4, 0),
                        RubbleShape.of(singleBlock(), 4, 5, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 5, 0, 0),
                        RubbleShape.of(singleBlock(), 5, 1, 0),
                        null,
                        RubbleShape.of(singleBlock(), 5, 3, 0),
                        RubbleShape.of(singleBlock(), 5, 4, 0),
                        RubbleShape.of(singleBlock(), 5, 5, 0))
        );

        gameScene.dropShape(Player.LEFT);

        /*
         * _ _ _ _ _ R
         * _ R R _ _ R
         * _ _ _ _ _ _
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         */
        assertThat(gameScene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 1, 1, 1).withInvisibleWallHorizontalOffset(5).withFallCausedBy(Player.LEFT),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 2, 1, 1).withInvisibleWallHorizontalOffset(5).withFallCausedBy(Player.LEFT),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 5, 0, 0),
                        RubbleShape.of(singleBlock(), 5, 1, 0),
                        null,
                        RubbleShape.of(singleBlock(), 5, 3, 0),
                        RubbleShape.of(singleBlock(), 5, 4, 0),
                        RubbleShape.of(singleBlock(), 5, 5, 0))
        );

        moveRubble();
        moveRubble();
        moveRubble();

        /*
         * _ _ _ _ _ R
         * _ _ _ R R R
         * _ _ _ _ _ _
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         */

        assertThat(gameScene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 3, 1, 0),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        RubbleShape.of(singleBlock(), 4, 1, 0),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        RubbleShape.of(singleBlock(), 5, 0, 0),
                        RubbleShape.of(singleBlock(), 5, 1, 0),
                        null,
                        RubbleShape.of(singleBlock(), 5, 3, 0),
                        RubbleShape.of(singleBlock(), 5, 4, 0),
                        RubbleShape.of(singleBlock(), 5, 5, 0))
        );
    }

    private void moveRubble() {
        gameScene.getRubble().stream().flatMap(Collection::stream).filter(Objects::nonNull).forEach(gameScene::moveRubble);
    }
}