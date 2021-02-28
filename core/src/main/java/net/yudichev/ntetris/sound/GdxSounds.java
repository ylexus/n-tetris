package net.yudichev.ntetris.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public final class GdxSounds implements Sounds {
    private final Map<Sample, Sound> soundBySample;

    public GdxSounds() {
        soundBySample = new EnumMap<>(Stream.of(Sounds.Sample.values())
                .collect(toMap(Function.identity(), sample -> Gdx.audio.newSound(Gdx.files.internal(sample.name() + ".wav")))));
    }

    @Override
    public void play(Sample sample) {
        soundBySample.get(sample).play();
    }

    @Override
    public void close() {
        soundBySample.values().forEach(Sound::dispose);
    }
}
