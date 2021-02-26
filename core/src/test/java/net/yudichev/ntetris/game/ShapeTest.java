package net.yudichev.ntetris.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.RectangularPattern.singleBlock;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.O;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ShapeTest {
    @ParameterizedTest
    @MethodSource
    void overlapsWith(Shape shapeDrop1, Shape shapeDrop2, boolean expectOverlap) {
        assertThat(shapeDrop1.overlapsWith(shapeDrop2)).isEqualTo(expectOverlap);
        assertThat(shapeDrop2.overlapsWith(shapeDrop1)).isEqualTo(expectOverlap);
    }

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
        Shape originalShape = Shape.of(pattern(
                row(X, X, O, O, O),
                row(O, X, X, X, X)
                ),
                2, 2, 1);
        Shape shape = originalShape
                .rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ _ _ _ _
         _ _ O _ _ _ _
         _ _ 0 0 X X _
         _ X X O _ _ _
         _ _ _ O _ _ _
         _ _ _ O _ _ _
         */
        assertThat(shape).isEqualTo(Shape.of(pattern(
                row(O, X),
                row(X, X),
                row(X, O),
                row(X, O),
                row(X, O)
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
        assertThat(shape).isEqualTo(Shape.of(pattern(
                row(X, X, X, X, O),
                row(O, O, O, X, X)
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
        assertThat(shape).isEqualTo(Shape.of(pattern(
                row(O, X),
                row(O, X),
                row(O, X),
                row(X, X),
                row(X, O)
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
        Shape originalShape = Shape.of(pattern(
                row(X),
                row(X),
                row(X),
                row(X)
                ),
                2, 2, 1);
        Shape shape = originalShape.rotate();
        /*
         _ _ _ _ _ _ _
         _ _ _ X _ _ _
         _ _ O 0 O O _
         _ _ _ X _ _ _
         _ _ _ X _ _ _
         */
        assertThat(shape).isEqualTo(Shape.of(pattern(
                row(X, X, X, X)
                ),
                3, 1, 1));
        shape = shape.rotate();
        assertThat(shape).isEqualTo(originalShape);
    }

    @Test
    void toSingleBlockShapes(@Mock Consumer<Shape> shapeConsumer) {
        Shape.of(pattern(
                row(X, X, O),
                row(O, X, X)), 2, 3, 0)
                .toSingleBlockShapes(shapeConsumer);
        verify(shapeConsumer).accept(Shape.of(singleBlock(), 2, 4, 0));
        verify(shapeConsumer).accept(Shape.of(singleBlock(), 2, 3, 0));
        verify(shapeConsumer).accept(Shape.of(singleBlock(), 3, 5, 0));
        verify(shapeConsumer).accept(Shape.of(singleBlock(), 3, 4, 0));
        verifyNoMoreInteractions(shapeConsumer);
    }

    private static Stream<Arguments> overlapsWith() {
        return Stream.of(
                Arguments.of(
                        Shape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        Shape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 2, -1),
                        false),
                Arguments.of(
                        Shape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        Shape.of(pattern(
                                row(O, O, X),
                                row(X, X, X)), 1, 0, -1),
                        false),
                Arguments.of(
                        Shape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        Shape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 1, 0, -1),
                        true),
                Arguments.of(
                        Shape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        Shape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 0, 3, -1),
                        false),
                Arguments.of(
                        Shape.of(pattern(
                                row(O, X, O),
                                row(O, X, O)), 0, 0, 1),
                        Shape.of(pattern(
                                row(X, O, X),
                                row(X, O, X)), 0, 0, -1),
                        false)
        );
    }
}