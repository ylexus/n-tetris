package net.yudichev.ntetris;

import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
interface BaseSettings {
    int sceneWidthBlocks();

    int sceneHeightBlocks();
}
