package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

import static org.immutables.value.Value.Default;
import static org.immutables.value.Value.Immutable;

@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@PublicImmutablesStyle
abstract class BaseRubbleShape extends Shape<RubbleShape> {
    @Override
    @Value.Derived
    public RectangularPattern pattern() {
        return RectangularPattern.singleBlock();
    }

    @Default
    public int invisibleWallX() {
        return -1;
    }

    public abstract Optional<Player> fallCausedBy();

    public RubbleShape stop() {
        return RubbleShape.builder()
                .from(this)
                .setSpeedX(0)
                .setInvisibleWallX(-1)
                .setFallCausedBy(Optional.empty())
                .build();
    }
}
