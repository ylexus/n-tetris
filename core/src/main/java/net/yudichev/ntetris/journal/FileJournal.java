package net.yudichev.ntetris.journal;

import net.yudichev.ntetris.GameControl;
import net.yudichev.ntetris.Settings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileJournal implements GameJournal {

    private final BufferedWriter writer;

    public FileJournal(Path path) {
        try {
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void settings(Settings settings) {
        try {
            writer.write("SGS=");
            writer.write(Integer.toString(settings.sceneWidthBlocks()));
            writer.write(',');
            writer.write(Integer.toString(settings.sceneHeightBlocks()));
            writer.write('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginFrame(double time) {
        try {
            writer.write("FRM=");
            writer.write(Double.toString(time));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void randomNextInt(int range, int result) {
        try {
            writer.write("RND=");
            writer.write(Integer.toString(range));
            writer.write(',');
            writer.write(Integer.toString(result));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void gameControlActive(GameControl gameControl) {
        try {
            writer.write("CRL=");
            writer.write(gameControl.name());
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
