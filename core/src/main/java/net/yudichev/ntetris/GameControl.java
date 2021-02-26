package net.yudichev.ntetris;

import net.yudichev.ntetris.util.Nullable;

import static com.badlogic.gdx.Input.Keys.*;

public enum GameControl {
    LEFT_PLAYER_UP(W),
    LEFT_PLAYER_DOWN(S),
    LEFT_PLAYER_ROTATE(A),
    LEFT_PLAYER_DROP(D),
    RIGHT_PLAYER_UP(O),
    RIGHT_PLAYER_DOWN(L),
    RIGHT_PLAYER_ROTATE(SEMICOLON),
    RIGHT_PLAYER_DROP(K),
    PAUSE(BACKSPACE),
    ;

    static final GameControl[] ALL_GAME_CONTROLS = values();

    private final int gdxKeyCode;

    GameControl(int gdxKeyCode) {
        this.gdxKeyCode = gdxKeyCode;
    }

    public int getGdxKeyCode() {
        return gdxKeyCode;
    }

    @Nullable
    public static GameControl gdxKeyCodeToControl(int gdxKeyCode) {
        for (GameControl gameControl : ALL_GAME_CONTROLS) {
            if (gameControl.gdxKeyCode == gdxKeyCode) {
                return gameControl;
            }
        }
        return null;
    }
}
