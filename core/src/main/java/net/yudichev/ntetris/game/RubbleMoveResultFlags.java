package net.yudichev.ntetris.game;

final class RubbleMoveResultFlags {

    private static final int FLAG_MOVED = 0x1;
    private static final int FLAG_REQUIRES_MORE_MOVES = 0x2;

    public static boolean isMoved(int flags) {
        return hasFlag(flags, FLAG_MOVED);
    }

    public static boolean requiresMoreMoves(int flags) {
        return hasFlag(flags, FLAG_REQUIRES_MORE_MOVES);
    }

    public static int withRequiresMoreMoves(int flags) {
        return flags | FLAG_REQUIRES_MORE_MOVES;
    }

    public static int withMoved(int flags) {
        return flags | FLAG_MOVED;
    }

    private static boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }
}
