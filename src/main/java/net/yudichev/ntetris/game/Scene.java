package net.yudichev.ntetris.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static com.google.common.base.Preconditions.*;

final class Scene {
    private static final Logger logger = LoggerFactory.getLogger(Scene.class);
    // leftmost row first, column indexes from top to bottom
    private final List<List<Shape>> rubble;
    private final int width;
    private final int height;
    private final Consumer<Shape> onRubbleAdded;
    private final Consumer<Shape> onRubbleRemoved;
    private final BiConsumer<Shape, Shape> onRubbleAmended;
    private final Map<Player, Shape> playerShapesByPlayer = new EnumMap<>(Player.class);
    private final List<List<Shape>> unmodifiableRubble;
    private final Map<Player, Shape> unmodifiablePlayerShapesByPLayer = Collections.unmodifiableMap(playerShapesByPlayer);

    Scene(int width, int height, Consumer<Shape> onRubbleAdded, Consumer<Shape> onRubbleRemoved, BiConsumer<Shape, Shape> onRubbleAmended) {
        rubble = new ArrayList<>(width);
        this.width = width;
        this.height = height;
        this.onRubbleAdded = checkNotNull(onRubbleAdded);
        this.onRubbleRemoved = checkNotNull(onRubbleRemoved);
        this.onRubbleAmended = checkNotNull(onRubbleAmended);
        for (var i = 0; i < width; i++) {
            var column = new ArrayList<Shape>(height);
            for (var j = 0; j < height; j++) {
                column.add(null);
            }
            rubble.add(column);
        }
        unmodifiableRubble = Collections.unmodifiableList(rubble);
    }

    public void addRubbleColumnInTheMiddle(int holeIndex) {
        for (var rowIdx = 0; rowIdx < height; rowIdx++) {
            if (rowIdx != holeIndex) {
                var shape = Shape.of(RectangularPattern.singleBlock(),
                        width / 2,
                        rowIdx,
                        0);
                rubble.get(width / 2).set(rowIdx, shape);
                onRubbleAdded.accept(shape);
            }
        }
    }

    public boolean attemptAddPlayerShape(Player player, Shape shape) {
        if (overlapsWithRubble(shape)) {
            return false;
        }

        for (var anotherPlayer : Player.ALL_PLAYERS) {
            var anotherShape = playerShapesByPlayer.get(anotherPlayer);
            if (anotherShape != null && anotherShape != shape) {
                if (shape.overlapsWith(anotherShape)) {
                    return false;
                }
            }
        }
        checkArgument(playerShapesByPlayer.put(player, shape) == null, "player %s already has a shape", player);
        return true;
    }

    public void movePlayerShapeVertically(Player player, int verticalOffset) {
        var playerShape = playerShapesByPlayer.get(player);
        if (playerShape != null) {
            var newVerticalOffset = playerShape.verticalOffset() + verticalOffset;
            if (newVerticalOffset >= 0 && newVerticalOffset + playerShape.pattern().width() <= height) {
                var candidateShape = playerShape.withVerticalOffset(newVerticalOffset);
                if (!overlapsWithRubble(candidateShape)) {
                    for (var anotherPlayer : Player.ALL_PLAYERS) {
                        if (anotherPlayer != player) {
                            var anotherPlayersShape = playerShapesByPlayer.get(anotherPlayer);
                            if (anotherPlayersShape != null && candidateShape.overlapsWith(anotherPlayersShape)) {
                                convertToRubble(player, "collapsed with another player when moving vertically");
                                convertToRubble(anotherPlayer, "collapsed with another player that was moving vertically");
                                return;
                            }
                        }
                    }
                    playerShapesByPlayer.put(player, candidateShape);
                }
            }
        }
    }

    @Nullable
    public ShapeLoweringResult dropShape(Player player) {
        var shapeDrop = playerShapesByPlayer.get(player);
        if (shapeDrop != null) {
            var shapeLoweringResult = ShapeLoweringResult.BECAME_RUBBLE;
            while ((shapeDrop = playerShapesByPlayer.get(player)) != null) {
                shapeLoweringResult = lowerShape(player, shapeDrop);
                logger.info("Player {} lower result {}", player, shapeLoweringResult);
                if (shapeLoweringResult != ShapeLoweringResult.LOWERED) {
                    break;
                }
            }
            return shapeLoweringResult;
        }
        return null;
    }

    public Map<Player, Shape> getPlayerShapesByPlayer() {
        return unmodifiablePlayerShapesByPLayer;
    }

    public List<List<Shape>> getRubble() {
        return unmodifiableRubble;
    }

    @Nullable
    public ShapeLoweringResult lowerShape(Player player) {
        var shapeDrop = playerShapesByPlayer.get(player);
        return shapeDrop == null ? null : lowerShape(player, shapeDrop);
    }

    public void deletePlayerShape(Player player) {
        playerShapesByPlayer.remove(player);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double blockToAbsHorizontal(double blocksRelativeToLeft) {
        return (blocksRelativeToLeft + 0.5) / width;
    }

    public double blockToAbsVertical(double blocksRelativeToTop) {
        return (blocksRelativeToTop + 0.5) / height;
    }

    public Shape moveRubble(Shape shape) {
        if (shape.horizontalSpeed() == 0) {
            return shape;
        }

        var candidateColIdx = shape.horizontalOffset() + shape.horizontalSpeed();
        if (
            // overlaps with invisible wall
                shape.invisibleWallHorizontalOffset() == candidateColIdx
                        // overlaps with rubble
                        || rubble.get(candidateColIdx).get(shape.verticalOffset()) != null
                        // overlaps with a player
                        || anyPLayerShapeHasBlockAt(candidateColIdx, shape.verticalOffset())) {
            // do not move, just stop it
            return Shape.builder()
                    .from(shape)
                    .setHorizontalSpeed(0)
                    .setInvisibleWallHorizontalOffset(-1)
                    .build();
        } else {
            // move the shape
            var newShape = shape.withHorizontalOffset(candidateColIdx);
            checkState(rubble.get(shape.horizontalOffset()).set(shape.verticalOffset(), null) == shape);
            checkState(rubble.get(newShape.horizontalOffset()).set(newShape.verticalOffset(), newShape) == null);
            return newShape;
        }
    }

    private boolean anyPLayerShapeHasBlockAt(int horizontalOffset, int verticalOffset) {
        for (var playerShape : playerShapesByPlayer.values()) {
            if (playerShape.hasElementAtAbsoluteCoordinates(verticalOffset, horizontalOffset)) {
                return true;
            }
        }
        return false;
    }

    private boolean overlapsWithRubble(Shape shape) {
        for (var rowIdx = shape.horizontalOffset(); rowIdx < shape.horizontalOffset() + shape.pattern().height(); rowIdx++) {
            var columns = rubble.get(rowIdx);
            for (var colIdx = shape.verticalOffset(); colIdx < shape.verticalOffset() + shape.pattern().width(); colIdx++) {
                var rubbleShape = columns.get(colIdx);
                // optimisation: knowing rubble shape is a single block, can be simple
                if (rubbleShape != null && shape.pattern().hasElementAt(rowIdx - shape.horizontalOffset(), colIdx - shape.verticalOffset())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ShapeLoweringResult lowerShape(Player player, Shape shape) {
        var movedShape = shape.move();
        // are we touching another shape in the direction of our movement?
        for (var anotherPlayer : Player.ALL_PLAYERS) {
            var anotherShape = playerShapesByPlayer.get(anotherPlayer);
            if (anotherShape != null && anotherShape != shape) {
                if (movedShape.overlapsWith(anotherShape)) {
                    convertToRubble(player, "collapsed with another player when lowering");
                    convertToRubble(anotherPlayer, "collapsed with another player that was lowering");
                }
            }
        }

        // if we are still not rubble, check if we are touching rubble
        if (playerShapesByPlayer.containsKey(player)) {
            if (overlapsWithRubble(movedShape)) {
                convertToRubble(player, "collapsed with rubble");
            }
        }

        // if we are still not rubble, check if we reached the edge
        if (playerShapesByPlayer.containsKey(player)) {
            if (movedShape.touchingEdge(width)) {
                playerShapesByPlayer.remove(player);
                return ShapeLoweringResult.REACHED_BOTTOM;
            }
            playerShapesByPlayer.put(player, movedShape);
            return ShapeLoweringResult.LOWERED;
        }
        return ShapeLoweringResult.BECAME_RUBBLE;
    }

    private void convertToRubble(Player player, String reason) {
        var playerShape = playerShapesByPlayer.remove(player);
        if (playerShape != null) {
            playerShape.toSingleBlockShapes(shape -> {
                rubble.get(shape.horizontalOffset()).set(shape.verticalOffset(), shape);
                onRubbleAdded.accept(shape);
            });
            logger.debug("player {} shape {} converted to rubble because {}", player, playerShape, reason);
            maybeCollapseRubble(player, playerShape.horizontalOffset(), playerShape.pattern().height());
        }
    }

    private void maybeCollapseRubble(Player player, int startingColIdx, int width) {
        int startColIdx;
        IntPredicate loopCondition;
        IntUnaryOperator loopStep;
        int rubbleDropSpeed;
        switch (player) {
            case LEFT -> {
                startColIdx = startingColIdx + width - 1;
                loopCondition = colIdx -> colIdx >= startingColIdx;
                loopStep = colIdx -> colIdx - 1;
                rubbleDropSpeed = 1;
            }
            case RIGHT -> {
                startColIdx = startingColIdx;
                loopCondition = colIdx -> colIdx < startingColIdx + width;
                loopStep = colIdx -> colIdx + 1;
                rubbleDropSpeed = -1;
            }
            default -> throw new IllegalStateException("invalid player " + player);
        }
        var firstCollapsedColIdx = -1;
        for (var colIdx = startColIdx; loopCondition.test(colIdx); colIdx = loopStep.applyAsInt(colIdx)) {
            var candidateRow = rubble.get(colIdx);
            var rowFull = true;
            for (var rowIdx = 0; rowIdx < candidateRow.size(); rowIdx++) {
                if (candidateRow.get(rowIdx) == null) {
                    rowFull = false;
                    break;
                }
            }
            if (rowFull) {
                logger.debug("Collapsed rubble column {}", colIdx);
                firstCollapsedColIdx = colIdx;
                for (var rowIdx = 0; rowIdx < candidateRow.size(); rowIdx++) {
                    onRubbleRemoved.accept(candidateRow.set(rowIdx, null));
                }
            }
        }
        if (firstCollapsedColIdx >= 0) {
            for (var colIdx = firstCollapsedColIdx; loopCondition.test(colIdx); colIdx = loopStep.applyAsInt(colIdx)) {
                var candidateRow = rubble.get(colIdx);
                for (var rowIdx = 0; rowIdx < candidateRow.size(); rowIdx++) {
                    var shape = candidateRow.get(rowIdx);
                    if (shape != null) {
                        var newShape = Shape.builder().from(shape)
                                .setHorizontalSpeed(rubbleDropSpeed)
                                // drop no deeper than the first collapsed row
                                .setInvisibleWallHorizontalOffset(Math.max(0, Math.min(firstCollapsedColIdx + rubbleDropSpeed, this.width - 1)))
                                .build();
                        candidateRow.set(rowIdx, newShape);
                        onRubbleAmended.accept(shape, newShape);
                    }
                }
            }
        }
    }

    public enum ShapeLoweringResult {
        LOWERED, BECAME_RUBBLE, REACHED_BOTTOM
    }
}
