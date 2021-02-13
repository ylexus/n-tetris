package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

@Immutable
@PublicImmutablesStyle
interface BasePlayerShapeState {
    @Value.Parameter
    Shape currentShape();

    Optional<Shape> nextShape();
}
