package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.yudichev.ntetris.GdxGame;

public final class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1280;
        config.height = 768;
        //noinspection ResultOfObjectAllocationIgnored
        new LwjglApplication(new GdxGame(), config);
    }
}
