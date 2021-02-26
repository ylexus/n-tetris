package net.yudichev.ntetris;

import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
interface BaseSettings {
    int playerZoneWidthInBlocks();

    int playerZoneHeightInBlocks();
}
