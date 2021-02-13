package net.yudichev.ntetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        scene = new Scene(10, 10, onRubbleAdded, onRubbleRemoved, onRubbleAmended);
    }

    @Test
    void collapseScenario() {
        scene.addRubbleColumnInTheMiddle(1);
        scene.attemptAddPlayerShape(Player.LEFT, Shape.of(PlayerShapeType.T.getPattern(), 0, 0, 1));
        scene.dropShape(Player.LEFT);
        assertThat(scene.getRubble().get(4)).contains(
                Shape.of(RectangularPattern.singleBlock(), 4, 0, 1).withInvisibleWallHorizontalOffset(6),
                Shape.of(RectangularPattern.singleBlock(), 4, 1, 1).withInvisibleWallHorizontalOffset(6),
                Shape.of(RectangularPattern.singleBlock(), 4, 2, 1).withInvisibleWallHorizontalOffset(6));
    }
}