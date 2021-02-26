package net.yudichev.ntetris.canvas;

import com.badlogic.gdx.graphics.Color;
import net.yudichev.ntetris.PublicImmutablesStyle;
import net.yudichev.ntetris.game.Shape;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

@Immutable
@PublicImmutablesStyle
abstract class BaseBlock {
    @Value.Parameter
    abstract Color color();

    @Value.Parameter
    abstract String name();

    abstract Optional<Shape> shape();
}
