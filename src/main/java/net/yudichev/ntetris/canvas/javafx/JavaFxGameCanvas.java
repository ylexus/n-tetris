package net.yudichev.ntetris.canvas.javafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.game.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JavaFxGameCanvas implements GameCanvas {
    private static final Logger logger = LoggerFactory.getLogger(JavaFxGameCanvas.class);

    private final GraphicsContext gc;
    private final double blockWidth;
    private final double blockHeight;
    private double canvasWidthPixels;
    private double canvasHeightPixels;
    private double blockWidthPixels;
    private double blockHeightPixels;

    public JavaFxGameCanvas(GraphicsContext gc, Settings settings) {
        this.gc = checkNotNull(gc);

        blockWidth = 1d / (settings.playerZoneWidthInBlocks() * 2);
        blockHeight = 1d / settings.playerZoneHeightInBlocks();
    }

    public void setSize(double canvasWidthPixels, double canvasHeightPixels) {
        logger.debug("Size changed to {}:{}", canvasWidthPixels, canvasHeightPixels);
        this.canvasWidthPixels = canvasWidthPixels;
        this.canvasHeightPixels = canvasHeightPixels;
        blockWidthPixels = toPixelsX(blockWidth);
        blockHeightPixels = toPixelsY(blockHeight);
    }

    @Override
    public void renderRubble(double x, double y) {
        gc.setFill(Color.BLUE);
        gc.fillRect(toPixelsX(x - blockWidth / 2), toPixelsY(y - blockHeight / 2), blockWidthPixels, blockHeightPixels);
    }

    @Override
    public void renderBlock(double x, double y, Player player) {
        gc.setFill(player == Player.LEFT ? Color.GREEN : Color.RED);
        gc.fillRect(toPixelsX(x - blockWidth / 2), toPixelsY(y - blockHeight / 2), blockWidthPixels, blockHeightPixels);
    }

    private double toPixelsX(double x) {
        return x * canvasWidthPixels;
    }

    private double toPixelsY(double y) {
        return y * canvasHeightPixels;
    }
}
