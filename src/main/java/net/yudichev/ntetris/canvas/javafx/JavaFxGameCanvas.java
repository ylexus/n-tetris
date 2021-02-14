package net.yudichev.ntetris.canvas.javafx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.Block;
import net.yudichev.ntetris.canvas.GameCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.text.TextAlignment.CENTER;
import static javafx.scene.text.TextAlignment.LEFT;

public final class JavaFxGameCanvas implements GameCanvas {
    private static final Logger logger = LoggerFactory.getLogger(JavaFxGameCanvas.class);

    private final GraphicsContext gc;
    private final double blockWidth;
    private final double blockHeight;
    private double canvasWidthPixels;
    private double canvasHeightPixels;
    private double blockWidthPixels;
    private double blockHeightPixels;
    private Font debugFont;
    private Font announcementFont;

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
        debugFont = new Font(gc.getFont().getName(), blockWidthPixels / 2);
        announcementFont = new Font(gc.getFont().getName(), blockWidthPixels * 5);
    }

    @Override
    public void renderBlock(double x, double y, Block block) {
        var pixelX = toPixelsX(x - blockWidth / 2);
        var pixelY = toPixelsY(y - blockHeight / 2);
        gc.setFill(block.color());
        gc.fillRect(pixelX, pixelY, blockWidthPixels, blockHeightPixels);
        block.shape().ifPresent(shape -> {
            if (shape.horizontalSpeed() != 0) {
                gc.setFont(debugFont);
                gc.setTextAlign(LEFT);
                gc.setFill(BLACK);
                gc.fillText(shape.horizontalSpeed() < 0 ? "←" : "→", pixelX, pixelY + blockHeightPixels);
            }
            if (shape.invisibleWallHorizontalOffset() >= 0) {
                var font = gc.getFont();
                gc.setFont(debugFont);
                gc.setTextAlign(LEFT);
                gc.setFill(BLACK);
                gc.fillText(String.valueOf(shape.invisibleWallHorizontalOffset()), pixelX + blockWidthPixels / 2, pixelY + blockHeightPixels);
            }
        });
    }

    @Override
    public void renderText(String text) {
        gc.setFill(BLACK);
        gc.setTextAlign(CENTER);
        gc.setFont(announcementFont);
        gc.fillText(text, toPixelsX(0.5), toPixelsY(0.5));
    }

    private double toPixelsX(double x) {
        return x * canvasWidthPixels;
    }

    private double toPixelsY(double y) {
        return y * canvasHeightPixels;
    }
}
