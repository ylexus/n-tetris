package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
abstract class BaseEffectShape extends Shape<EffectShape> {
    @Override
    @Value.Derived
    public RectangularPattern pattern() {
        return RectangularPattern.singleBlock();
    }
}
