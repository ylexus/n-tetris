package net.yudichev.ntetris.canvas;

import net.yudichev.ntetris.PublicImmutablesStyle;
import net.yudichev.ntetris.canvas.game.BlockLook;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
abstract class BaseBlock {
    @Value.Parameter
    abstract BlockLook look();
}
