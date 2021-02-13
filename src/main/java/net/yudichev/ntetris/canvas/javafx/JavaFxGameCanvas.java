package net.yudichev.ntetris.canvas.javafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javafx.scene.text.TextAlignment.CENTER;

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

        blockWidth = 1.0d / (settings.playerZoneWidthInBlocks() * 2);
        blockHeight = 1.0d / settings.playerZoneHeightInBlocks();
    }

    public void setSize(double canvasWidthPixels, double canvasHeightPixels) {
        logger.debug("Size changed to {}:{}", canvasWidthPixels, canvasHeightPixels);
        this.canvasWidthPixels = canvasWidthPixels;
        this.canvasHeightPixels = canvasHeightPixels;
        blockWidthPixels = toPixelsX(blockWidth);
        blockHeightPixels = toPixelsY(blockHeight);
    }

    @Override
    public void renderBlock(double x, double y, Block block) {
        gc.setFill(block.getColor());
        gc.fillRect(toPixelsX(x - blockWidth / 2), toPixelsY(y - blockHeight / 2), blockWidthPixels, blockHeightPixels);
    }

    @Override
    public void renderGameOver() {
        gc.setFill(Color.BLACK);
        gc.setTextAlign(CENTER);
        gc.fillText("Game Over", toPixelsX(0.5), toPixelsY(0.5));
    }

    private double toPixelsX(double x) {
        return x * canvasWidthPixels;
    }

    private double toPixelsY(double y) {
        return y * canvasHeightPixels;
    }
}
