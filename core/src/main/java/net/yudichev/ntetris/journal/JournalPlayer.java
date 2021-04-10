package net.yudichev.ntetris.journal;

import net.yudichev.ntetris.ControlState;
import net.yudichev.ntetris.GameControl;
import net.yudichev.ntetris.RandomNumberGenerator;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.util.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.DoubleConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.yudichev.ntetris.util.MoreThrowables.asUnchecked;
import static net.yudichev.ntetris.util.MoreThrowables.getAsUnchecked;
import static net.yudichev.ntetris.util.Preconditions.*;

public final class JournalPlayer {
    private static final String SETTINGS = "SGS=";
    private static final String FRAME_START = "FRM=";
    private static final String CONTROL = "CRL=";
    private static final String RANDOM = "RND=";

    private final BufferedReader reader;
    private final DoubleConsumer frameStartConsumer;
    private final Settings settings;
    private double gameTime;
    private String pendingLine;

    public JournalPlayer(String resourcePath, DoubleConsumer frameStartConsumer) {
        InputStream resourceAsStream = checkNotNull(getClass().getClassLoader().getResourceAsStream(resourcePath),
                "Resource not found: %s", resourcePath);
        reader = new BufferedReader(new InputStreamReader(resourceAsStream, UTF_8));
        this.frameStartConsumer = checkNotNull(frameStartConsumer);
        try {
            String settingsStr = readAndConsumeLine();
            checkArgument(settingsStr != null && settingsStr.startsWith(SETTINGS));
            int splitterIndex = settingsStr.indexOf(',');
            settings = Settings.builder()
                    .setSceneWidthBlocks(Integer.parseInt(settingsStr.substring(SETTINGS.length(), splitterIndex)))
                    .setSceneHeightBlocks(Integer.parseInt(settingsStr.substring(splitterIndex + 1)))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Settings settings() {
        return settings;
    }

    public ControlState controlState() {
        return (gameTime, activeControlConsumer) -> asUnchecked(() -> {
            String line = readNextLine();
            if (line != null && line.startsWith(CONTROL)) {
                consumeLine();
                //noinspection FloatingPointEquality as intended
                checkState(gameTime == this.gameTime, "Unexpected game time in %s: expected %s, was %s", CONTROL, this.gameTime, gameTime);
                activeControlConsumer.accept(GameControl.valueOf(line.substring(CONTROL.length())));
            }
        });
    }

    public RandomNumberGenerator randomNumberGenerator() {
        return range -> getAsUnchecked(() -> {
            String line = readAndConsumeLine();
            checkState(line != null, "Expected %s, but was EOF", RANDOM);
            checkState(line.startsWith(RANDOM), "Expected %s, but was %s", RANDOM, line);
            int delimiterIndex = line.indexOf(',');
            int expectedRange = Integer.parseInt(line.substring(RANDOM.length(), delimiterIndex));
            checkState(expectedRange == range, "Expected range in %s = %s but was %s", RANDOM, range, line);
            return Integer.parseInt(line.substring(delimiterIndex + 1));
        });
    }

    public void play() {
        asUnchecked(() -> {
            while (readNextLine() != null) {
                String line = consumeLine();
                if (line.startsWith(FRAME_START)) {
                    gameTime = Double.parseDouble(line.substring(FRAME_START.length()));
                    frameStartConsumer.accept(gameTime);
                } else {
                    throw new IllegalStateException("Unexpected journal entry, expected " + FRAME_START + " but was " + line);
                }
            }
        });
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private String readNextLine() throws IOException {
        if (pendingLine == null) {
            pendingLine = reader.readLine();
        }
        return pendingLine;
    }

    private String consumeLine() {
        checkState(pendingLine != null);
        String result = pendingLine;
        pendingLine = null;
        return result;
    }

    @Nullable
    private String readAndConsumeLine() throws IOException {
        String line = readNextLine();
        if (line == null) {
            return null;
        } else {
            return consumeLine();
        }
    }
}
