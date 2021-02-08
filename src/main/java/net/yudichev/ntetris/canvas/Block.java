package net.yudichev.ntetris.canvas;

import net.yudichev.ntetris.game.Player;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Block {
    private final Player owner;

    protected Block(Player owner) {
        this.owner = checkNotNull(owner);
    }

    public Player getOwner() {
        return owner;
    }
}
