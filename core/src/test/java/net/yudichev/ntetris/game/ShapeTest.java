package net.yudichev.ntetris.game;

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
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 2, -1),
                        false),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(O, O, X),
                                row(X, X, X)), 1, 0, -1),
                        false),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 1, 0, -1),
                        true),
                Arguments.of(
                        PlayerShape.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        PlayerShape.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 0, 3, -1),
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
}