package net.yudichev.ntetris.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.O;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShapeTest {
    @ParameterizedTest
    @MethodSource
    void overlapsWith(PlayerShape shape1, PlayerShape shape2, boolean expectOverlap) {
        assertThat(shape1.overlapsWith(shape2)).isEqualTo(expectOverlap);
        assertThat(shape2.overlapsWith(shape1)).isEqualTo(expectOverlap);
    }

    private static Stream<Arguments> overlapsWith() {
        return Stream.of(
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 2, 0, 1),
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, -1),
                        false),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(O, O, X),
                                row(X, X, X)), 0, 1, -1),
                        false),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 0, 1, -1),
                        true),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 3, 0, 1),
                        PlayerShape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 0, 0, -1),
                        false),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, O),
                                row(O, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(X, O, X),
                                row(X, O, X)), 0, 0, -1),
                        false)
        );
    }

    @Test
    void hasElementAtAbsoluteCoordinates1() {
        /*
          X O
          X X
          O X
         */
        PlayerShape shape = PlayerShape.of(pattern(
                row(X, O),
                row(X, X),
                row(O, X)
        ), 0, 0, 0);
        assertThat(shape.hasElementAtAbsoluteCoordinates(0, 0)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(0, 1)).isFalse();
        assertThat(shape.hasElementAtAbsoluteCoordinates(1, 0)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(1, 1)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(2, 0)).isFalse();
        assertThat(shape.hasElementAtAbsoluteCoordinates(2, 1)).isTrue();
    }

    @Test
    void hasElementAtAbsoluteCoordinates2() {
        /*
          O X X
          O O X
          O O X
         */
        PlayerShape shape = PlayerShape.of(pattern(
                row(X, X),
                row(O, X),
                row(O, X)
        ), 1, 0, -1);
        assertThat(shape.hasElementAtAbsoluteCoordinates(0, 1)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(0, 2)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(1, 1)).isFalse();
        assertThat(shape.hasElementAtAbsoluteCoordinates(1, 2)).isTrue();
        assertThat(shape.hasElementAtAbsoluteCoordinates(2, 1)).isFalse();
        assertThat(shape.hasElementAtAbsoluteCoordinates(2, 2)).isTrue();
    }

    @Test
    void touchingVerticalRightEdge() {
        PlayerShape shape = PlayerShape.of(pattern(
                row(X, X),
                row(X, X),
                row(X, X),
                row(X, X)
        ), 1, 0, -1);
        assertThat(shape.touchingVerticalEdge(3)).isTrue();
        assertThat(shape.touchingVerticalEdge(4)).isFalse();
    }

    @Test
    void touchingVerticalLefgEdge() {
        PlayerShape shape = PlayerShape.of(pattern(
                row(X, X),
                row(X, X),
                row(X, X),
                row(X, X)
        ), 0, 0, -1);
        assertThat(shape.touchingVerticalEdge(3)).isTrue();
    }
}