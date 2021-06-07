package net.yudichev.ntetris;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
interface BaseSettings {
    int sceneWidthBlocks();

    int sceneHeightBlocks();

    @Value.Default
    default double accelerationRatePerMinute() {
        return 1.05;
    }
}
