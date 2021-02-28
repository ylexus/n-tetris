package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;

import java.util.Optional;

import static org.immutables.value.Value.Default;
import static org.immutables.value.Value.Immutable;

@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@PublicImmutablesStyle
abstract class BaseRubbleShape extends Shape<RubbleShape> {

    @Default
    public int invisibleWallHorizontalOffset() {
        return -1;
    }

    public abstract Optional<Player> fallCausedBy();

    public RubbleShape stopFalling() {
        return RubbleShape.builder()
                .from(this)
                .setHorizontalSpeed(0)
                .setInvisibleWallHorizontalOffset(-1)
                .setFallCausedBy(Optional.empty())
                .build();
    }
}
