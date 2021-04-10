package net.yudichev.ntetris;

import net.yudichev.ntetris.journal.GameJournal;

public final class NoopGameJournal implements GameJournal {
    @Override
    public void beginFrame(double time) {
    }

    @Override
    public void randomNextInt(int range, int result) {
    }

    @Override
    public void gameControlActive(GameControl gameControl) {
    }

    @Override
    public void settings(Settings settings) {
    }
}
