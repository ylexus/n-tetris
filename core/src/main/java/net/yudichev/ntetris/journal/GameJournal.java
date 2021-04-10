package net.yudichev.ntetris.journal;

import net.yudichev.ntetris.GameControl;
import net.yudichev.ntetris.Settings;

public interface GameJournal {

    void beginFrame(double time);

    void randomNextInt(int range, int result);

    void gameControlActive(GameControl gameControl);

    void settings(Settings settings);
}
