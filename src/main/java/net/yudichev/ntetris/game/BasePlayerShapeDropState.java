package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

@Immutable
@PublicImmutablesStyle
interface BasePlayerShapeDropState {
    @Value.Parameter
    ShapeDrop currentDrop();

    Optional<ShapeDrop> nextDrop();
}
