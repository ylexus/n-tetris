package net.yudichev.ntetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.RectangularPattern.singleBlock;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SceneTest {
    @Mock
    Consumer<Shape> onRubbleAdded;
    @Mock
    Consumer<Shape> onRubbleRemoved;
    @Mock
    BiConsumer<Shape, Shape> onRubbleAmended;
    private Scene scene;

    @BeforeEach
    void setUp() {
    }

    @Test
    void collapseScenario2() {
        scene = new Scene(4, 4, onRubbleAdded, onRubbleRemoved, onRubbleAmended);

        /*
         * _ R R R
         * R R R R
         * R R R R
         * R _ _ _
         */
        scene.addRubbleColumnWithHole(0, 0);
        scene.addRubbleColumnWithHole(1, 3);
        scene.addRubbleColumnWithHole(2, 3);
        scene.addRubbleColumnWithHole(3, 3);


        /*
         * _ R R R
         * R R R R
         * R R R R
         * R B B _
         */
        scene.attemptAddPlayerShape(Player.RIGHT, Shape.of(pattern(
                row(X),
                row(X)
        ), 1, 3, -1));
        scene.dropShape(Player.RIGHT);

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
        assertThat(scene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 0, 1, 0),
                        Shape.of(singleBlock(), 0, 2, 0),
                        Shape.of(singleBlock(), 0, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        Shape.of(singleBlock(), 1, 0, 0),
                        Shape.of(singleBlock(), 1, 1, 0),
                        Shape.of(singleBlock(), 1, 2, 0),
                        null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null)
        );
    }

    @Test
    void collapseScenario3_DoesNotDropUnrelatedRubble() {
        scene = new Scene(4, 4, onRubbleAdded, onRubbleRemoved, onRubbleAmended);

        /*
         * _ R _ R
         * R R _ R
         * R R _ R
         * R _ _ _
         */
        scene.addRubbleColumnWithHole(0, 0);
        scene.addRubbleColumnWithHole(1, 3);
        scene.addRubbleColumnWithHole(3, 3);

        /*
         * _ R _ R
         * R R _ R
         * R R _ R
         * R B B _
         */
        scene.attemptAddPlayerShape(Player.RIGHT, Shape.of(pattern(
                row(X),
                row(X)
        ), 1, 3, -1));
        scene.dropShape(Player.RIGHT);

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
        assertThat(scene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 0, 1, 0),
                        Shape.of(singleBlock(), 0, 2, 0),
                        Shape.of(singleBlock(), 0, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null,
                        Shape.of(singleBlock(), 1, 3, 0)),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        Shape.of(singleBlock(), 3, 0, 0),
                        Shape.of(singleBlock(), 3, 1, 0),
                        Shape.of(singleBlock(), 3, 2, 0),
                        null)
        );
    }

    @Test
    void collapseScenario() {
        scene = new Scene(6, 6, onRubbleAdded, onRubbleRemoved, onRubbleAmended);

        /*
         * _ _ _ R R R
         * _ _ _ _ _ R
         * _ _ _ R R _
         * _ _ _ R R R
         * _ _ _ R R R
         * _ _ _ R R R
         */
        scene.addRubbleColumnWithHole(scene.getWidth() / 2, 1);
        scene.addRubbleColumnWithHole(scene.getWidth() / 2 + 1, 1);
        scene.addRubbleColumnWithHole(scene.getWidth() / 2 + 2, 2);
        scene.attemptAddPlayerShape(Player.LEFT, Shape.of(PlayerShapeType.I.getPattern(), 0, 1, 1));
        scene.dropShape(Player.LEFT);

        /*
         * _ _ _ R R R
         * _ B B B B R
         * _ _ _ R R _
         * _ _ _ R R R
         * _ _ _ R R R
         * _ _ _ R R R
         */

        /*
         * _ _ _ _ _ R
         * _ R R _ _ R
         * _ _ _ _ _ _
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         * _ _ _ _ _ R
         */
        assertThat(scene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 1, 1, 1).withInvisibleWallHorizontalOffset(5).withFallCausedBy(Player.LEFT),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 2, 1, 1).withInvisibleWallHorizontalOffset(5).withFallCausedBy(Player.LEFT),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        Shape.of(singleBlock(), 5, 0, 0),
                        Shape.of(singleBlock(), 5, 1, 0),
                        null,
                        Shape.of(singleBlock(), 5, 3, 0),
                        Shape.of(singleBlock(), 5, 4, 0),
                        Shape.of(singleBlock(), 5, 5, 0))
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

        assertThat(scene.getRubble()).satisfiesExactly(
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null, null, null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 3, 1, 0),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 4, 1, 0),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        Shape.of(singleBlock(), 5, 0, 0),
                        Shape.of(singleBlock(), 5, 1, 0),
                        null,
                        Shape.of(singleBlock(), 5, 3, 0),
                        Shape.of(singleBlock(), 5, 4, 0),
                        Shape.of(singleBlock(), 5, 5, 0))
        );
    }

    private void moveRubble() {
        scene.getRubble().stream().flatMap(Collection::stream).filter(Objects::nonNull).forEach(scene::moveRubble);
    }
}