package net.yudichev.ntetris.game;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.yudichev.ntetris.game.RectangularPattern.pattern;
import static net.yudichev.ntetris.game.Row.row;
import static net.yudichev.ntetris.game.ShapeConstants.O;
import static net.yudichev.ntetris.game.ShapeConstants.X;
import static org.assertj.core.api.Assertions.assertThat;

class ShapeDropTest {
    @ParameterizedTest
    @MethodSource
    void overlapsWith(ShapeDrop shapeDrop1, ShapeDrop shapeDrop2, boolean expectOverlap) {
        assertThat(shapeDrop1.overlapsWithAnotherDrop(shapeDrop2)).isEqualTo(expectOverlap);
        assertThat(shapeDrop2.overlapsWithAnotherDrop(shapeDrop1)).isEqualTo(expectOverlap);
    }

    @ParameterizedTest
    @MethodSource
    void overlapsWithRubble(ShapeDrop shapeDrop, List<Row> rubble, boolean expectOverlap) {
        assertThat(shapeDrop.overlapsWithRubble(rubble)).isEqualTo(expectOverlap);
    }

    private static Stream<Arguments> overlapsWith() {
        return Stream.of(
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 2, -1),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        ShapeDrop.of(pattern(
                                row(O, O, X),
                                row(X, X, X)), 1, 0, -1),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        ShapeDrop.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 1, 0, -1),
                        true),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        ShapeDrop.of(pattern(
                                row(X, O, X),
                                row(X, X, X)), 0, 3, -1),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, O),
                                row(O, X, O)), 0, 0, 1),
                        ShapeDrop.of(pattern(
                                row(X, O, X),
                                row(X, O, X)), 0, 0, -1),
                        false)
        );
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument") // for consistency
    private static Stream<Arguments> overlapsWithRubble() {
        return Stream.of(
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        asList(row(X, O, O)),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        asList(row(X, O, O),
                               row(O, O, X)),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, O, O)), 0, 1, 1),
                        asList(row(X, O, O),
                               row(O, O, X)),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, O, O)), 0, 0, 1),
                        asList(row(X, O),
                               row(O, X)),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        asList(row(X, O, X)),
                        true),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, X, O)), 0, 0, 1),
                        asList(row(X, O, O),
                               row(O, X, X)),
                        true),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, O, O)), 0, 1, 1),
                        asList(row(X, O, X),
                               row(O, O, X)),
                        true),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(O, X, X),
                                row(X, O, O)), 0, 0, 1),
                        asList(row(X, O),
                               row(X, X)),
                        true),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(X, X, X),
                                row(O, X, O)), 1, 1, 1),
                        asList(row(X, X, X, X, X),
                               row(X, O, O, O, X),
                               row(X, X, O, X, X)),
                        false),
                Arguments.of(
                        ShapeDrop.of(pattern(
                                row(X, X, X),
                                row(O, X, O)), 1, 2, 1),
                        asList(row(X, X, X, X, X),
                               row(X, O, O, O, X),
                               row(X, X, O, X, X)),
                        true)
        );
    }
}