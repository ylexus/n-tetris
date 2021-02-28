package net.yudichev.ntetris.sound;

public interface Sounds {
    void play(Sample sample);

    void close();

    enum Sample {
        RUBBLE_COLLAPSE,
    }
}
