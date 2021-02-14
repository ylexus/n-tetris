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

import static net.yudichev.ntetris.game.RectangularPattern.singleBlock;
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
        scene = new Scene(6, 6, onRubbleAdded, onRubbleRemoved, onRubbleAmended);
    }

    @Test
    void collapseScenario() {
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
                        Shape.of(singleBlock(), 1, 1, 1).withInvisibleWallHorizontalOffset(5),
                        null, null, null, null),
                shapes -> assertThat(shapes).containsExactly(
                        null,
                        Shape.of(singleBlock(), 2, 1, 1).withInvisibleWallHorizontalOffset(5),
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