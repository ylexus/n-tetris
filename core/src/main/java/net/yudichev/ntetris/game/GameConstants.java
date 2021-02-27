package net.yudichev.ntetris.game;

public final class GameConstants {
    public static final double DROP_STEP_DURATION_PLAYER = 1000;
    public static final double DROP_STEP_DURATION_RUBBLE = 200;
    public static final double PLAYER_PENALTY_PAUSE = DROP_STEP_DURATION_PLAYER * 3;
    public static final double DROP_TRANSITION_STEP_DURATION = 100;

    private GameConstants() {
    }
}
