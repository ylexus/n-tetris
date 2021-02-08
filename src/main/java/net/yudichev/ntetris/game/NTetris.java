package net.yudichev.ntetris.game;

import javafx.scene.input.KeyEvent;
import net.yudichev.ntetris.Game;
import net.yudichev.ntetris.Settings;
import net.yudichev.ntetris.canvas.GameCanvas;

import java.util.List;
import java.util.Random;

// all time units are millis
// all coordinates are relative (0...1) except when mentioned
public final class NTetris implements Game {
    private static final long DROP_STEP_DURATION = 200;
    private static final Shape[] ALL_SHAPES = Shape.values();
    private final Random random = new Random();

    private final Scene scene;
    private final Settings settings;
    private final int sceneWidthBlocks;
    private final int sceneHeightBlocks;

    private double lastStepTime = Double.MIN_VALUE;

    public NTetris(Settings settings) {
        sceneWidthBlocks = settings.playerZoneWidthInBlocks() * 2 + 1;
        sceneHeightBlocks = settings.playerZoneHeightInBlocks();
        scene = new Scene(sceneWidthBlocks, sceneHeightBlocks);
        this.settings = settings;
        for (Player allPlayer : Player.ALL_PLAYERS) {
            spawnNewShape(allPlayer);
        }
//        scene.addRubble(sceneWidthBlocks / 2, fullRow(sceneHeightBlocks));
    }

    @Override
    public void render(double gameTimeMillis, long frameNumber, GameCanvas canvas, List<KeyEvent> keyEventQueue) {
        if (lastStepTime == Double.MIN_VALUE) {
            lastStepTime = gameTimeMillis;
            return;
        }

        for (KeyEvent keyEvent : keyEventQueue) {
            switch (keyEvent.getCode()) {
                case W -> scene.movePlayerShapeVertically(Player.LEFT, -1);
                case S -> scene.movePlayerShapeVertically(Player.LEFT, 1);
                case O -> scene.movePlayerShapeVertically(Player.RIGHT, -1);
                case L -> scene.movePlayerShapeVertically(Player.RIGHT, 1);
            }
            keyEvent.consume();
        }

        var timeSinceLastStep = gameTimeMillis - lastStepTime;
        long outstandingSteps = (long) Math.floor(timeSinceLastStep / DROP_STEP_DURATION);
        if (outstandingSteps > 0) {
            lastStepTime = gameTimeMillis;
        }
        while (outstandingSteps-- > 0) {
            scene.fallShapes();
        }
        var shapeDropsByPlayer = scene.getShapeDropsByPlayer();
        for (int i = 0; i < Player.ALL_PLAYERS.length; i++) {
            Player player = Player.ALL_PLAYERS[i];
            var playerShapeDrop = shapeDropsByPlayer.get(player);
            if (playerShapeDrop != null) {
                renderShape(canvas, player, playerShapeDrop);
            } else {
                spawnNewShape(player);
            }
        }
        renderRubble(canvas);
    }

    private void spawnNewShape(Player player) {
        // TODO may not able to spawn
        var shape = ALL_SHAPES[random.nextInt(ALL_SHAPES.length)];
        int horizontalOffset;
        int horizontalSpeed;
        switch (player) {
            case LEFT -> {
                horizontalOffset = 0;
                horizontalSpeed = 1;
            }
            case RIGHT -> {
                horizontalOffset = sceneWidthBlocks - shape.getPattern().height();
                horizontalSpeed = -1;
            }
            default -> throw new IllegalArgumentException();
        }
        scene.addPlayerShape(player, ShapeDrop.of(shape.getPattern(), horizontalOffset, 5, horizontalSpeed));
    }

    private void renderShape(GameCanvas canvas, Player player, BaseShapeDrop shapeDrop) {
        for (int rowIdx = 0; rowIdx < shapeDrop.shape().getRows().size(); rowIdx++) {
            Row row = shapeDrop.shape().getRows().get(rowIdx);
            var absRowIdx = rowIdx + shapeDrop.horizontalOffset();
            for (int colIdx = 0; colIdx < row.getElements().length; colIdx++) {
                if (row.getElements()[colIdx]) {
                    var absColIdx = colIdx + shapeDrop.verticalOffset();
                    canvas.renderBlock(blockToAbsHorizontal(absRowIdx), blockToAbsVertical(absColIdx), player);
                }
            }
        }
    }

    private void renderRubble(GameCanvas canvas) {
        for (int rowIdx = 0; rowIdx < scene.getRubble().size(); rowIdx++) {
            Row row = scene.getRubble().get(rowIdx);
            var rowElements = row.getElements();
            for (int colIdx = 0; colIdx < rowElements.length; colIdx++) {
                boolean element = rowElements[colIdx];
                if (element) {
                    canvas.renderRubble(blockToAbsHorizontal(rowIdx), blockToAbsVertical(colIdx));
                }
            }
        }
    }

    private double blockToAbsHorizontal(int blocksRelativeToLeft) {
        return (blocksRelativeToLeft + 0.5) / sceneWidthBlocks;
    }

    private double blockToAbsVertical(int blocksRelativeToTop) {
        return (blocksRelativeToTop + 0.5) / sceneHeightBlocks;
    }
}
