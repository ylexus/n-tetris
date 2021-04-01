package net.yudichev.ntetris.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.O;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
final class PlayerShapeTest {
    @Test
    void rotate() {
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ X _ _ _ _
         _ _ X X _ _ _
         _ _ _ X _ _ _
         _ _ _ X _ _ _
         _ _ _ X _ _ _
         */
        PlayerShape originalShape = PlayerShape.of(pattern(
                row(X, O),
                row(X, X),
                row(O, X),
                row(O, X),
                row(O, X)
        ), 2, 2, 1);
        PlayerShape shape = originalShape.rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ O _ _ _ _
         _ _ 0 0 X X _
         _ X X O _ _ _
         _ _ _ O _ _ _
         _ _ _ O _ _ _
         */
        assertThat(shape).isEqualTo(PlayerShape.of(pattern(
                row(O, X, X, X, X),
                row(X, X, O, O, O)
                ),
                1, 3, 1));
        shape = shape.rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ X _ _ _ _
         _ _ X O _ _ _
         _ _ X O _ _ _
         _ _ X 0 _ _ _
         _ _ _ 0 _ _ _
         */
        assertThat(shape).isEqualTo(PlayerShape.of(pattern(
                row(X, O),
                row(X, O),
                row(X, O),
                row(X, X),
                row(O, X)
                ),
                2, 2, 1));

        shape = shape.rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ O _ _ _ _
         _ _ O O X X _
         _ X X 0 X _ _
         _ _ _ O _ _ _
         _ _ _ O _ _ _
         */
        assertThat(shape).isEqualTo(PlayerShape.of(pattern(
                row(O, O, O, X, X),
                row(X, X, X, X, O)
                ),
                1, 3, 1));

        assertThat(shape.rotate()).isEqualTo(originalShape);
    }

    @Test
    void rotate2() {
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ X X X X _
         _ _ _ _ _ _ _
         */
        PlayerShape originalShape = PlayerShape.of(pattern(
                row(X, X, X, X)
                ),
                2, 2, 1);
        PlayerShape shape = originalShape.rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ X _ _ _
         _ _ O 0 O O _
         _ _ _ X _ _ _
         _ _ _ X _ _ _
         */
        assertThat(shape).isEqualTo(PlayerShape.of(pattern(
                row(X),
                row(X),
                row(X),
                row(X)
                ),
                3, 1, 1));
        shape = shape.rotate();
        assertThat(shape).isEqualTo(originalShape);
    }

    @Test
    void toSingleBlockShapes(@Mock Consumer<RubbleShape> shapeConsumer) {
        /*
          O O O O
          O O O O
          O O O O
          O O O X
              X X
              X O
         */
        PlayerShape.of(pattern(
                row(O, X),
                row(X, X),
                row(X, O)
        ), 2, 3, 0)
                .toSingleBlockShapes(shapeConsumer);
        verify(shapeConsumer).accept(RubbleShape.of(3, 3, 0));
        verify(shapeConsumer).accept(RubbleShape.of(2, 4, 0));
        verify(shapeConsumer).accept(RubbleShape.of(3, 4, 0));
        verify(shapeConsumer).accept(RubbleShape.of(2, 5, 0));
        verifyNoMoreInteractions(shapeConsumer);
    }
}