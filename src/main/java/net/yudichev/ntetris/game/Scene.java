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
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.fill;

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
    private final boolean[] collapsingRubbleClusterEndedRowIndexes;

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
        collapsingRubbleClusterEndedRowIndexes = new boolean[height];
    }

    public void addRubbleColumnWithHole(int colIdx, int holeIndex) {
        for (var rowIdx = 0; rowIdx < height; rowIdx++) {
            if (rowIdx != holeIndex) {
                var shape = Shape.of(RectangularPattern.singleBlock(),
                        colIdx,
                        rowIdx,
                        0);
                rubble.get(colIdx).set(rowIdx, shape);
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

    public void moveRubble(Shape shape) {
        var newShape = shape;
        if (shape.horizontalSpeed() != 0) {
            var candidateColIdx = shape.horizontalOffset() + shape.horizontalSpeed();
            if (shape.invisibleWallHorizontalOffset() == candidateColIdx) {
                // overlaps with invisible wall
                // do not move, just stop it
                newShape = shape.stopFalling();
            } else {
                var overlappingRubble = rubble.get(candidateColIdx).get(shape.verticalOffset());
                if (overlappingRubble != null) {
                    if (!movingInSameDirection(shape, overlappingRubble)) {
                        newShape = shape.stopFalling();
                    }
                } else {
                    var overlappingPlayerShape = playerShapeWithElementAt(candidateColIdx, shape.verticalOffset(), shape.horizontalSpeed());
                    if (overlappingPlayerShape != null) {
                        if (!movingInSameDirection(shape, overlappingPlayerShape)) {
                            newShape = shape.stopFalling();
                        }
                    } else {
                        // move the shape
                        newShape = shape.withHorizontalOffset(candidateColIdx);
                    }
                }
            }
        }
        if (newShape != shape) {
            checkState(rubble.get(shape.horizontalOffset()).set(shape.verticalOffset(), null) == shape);
            checkState(rubble.get(newShape.horizontalOffset()).set(newShape.verticalOffset(), newShape) == null);
            onRubbleAmended.accept(shape, newShape);
            var newShapePosition = newShape.horizontalOffset();
            newShape.fallCausedBy().ifPresent(player -> maybeCollapseRubble(player, newShapePosition, 1));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // easier to read this way
    private static boolean movingInSameDirection(Shape shape1, Shape shape2) {
        return Integer.signum(shape1.horizontalSpeed()) == Integer.signum(shape2.horizontalSpeed());
    }

    @Nullable
    private Shape playerShapeWithElementAt(int horizontalOffset, int verticalOffset, int friendlySpeed) {
        checkArgument(friendlySpeed != 0);
        for (var playerShape : playerShapesByPlayer.values()) {
            if (playerShape.hasElementAtAbsoluteCoordinates(verticalOffset, horizontalOffset)) {
                return playerShape;
            }
        }
        return null;
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
            if (anotherPlayer != player) {
                var anotherShape = playerShapesByPlayer.get(anotherPlayer);
                if (anotherShape != null) {
                    if (movedShape.overlapsWith(anotherShape)) {
                        convertToRubble(player, "collapsed with another player when lowering");
                        convertToRubble(anotherPlayer, "collapsed with another player that was lowering");
                    }
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
            if (movedShape.touchingVerticalEdge(width)) {
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
        IntPredicate collapseRangeCondition;
        IntPredicate movingRangeCondition;
        IntUnaryOperator loopStep;
        IntBiPredicate furtherThanTest;
        int rubbleDropSpeed;
        switch (player) {
            case LEFT -> {
                startColIdx = startingColIdx + width - 1;
                collapseRangeCondition = colIdx -> colIdx >= startingColIdx;
                movingRangeCondition = colIdx -> colIdx >= 0;
                loopStep = colIdx -> colIdx - 1;
                furtherThanTest = (first, second) -> first < second;
                rubbleDropSpeed = 1;
            }
            case RIGHT -> {
                startColIdx = startingColIdx;
                collapseRangeCondition = colIdx -> colIdx < startingColIdx + width;
                movingRangeCondition = colIdx -> colIdx < this.width;
                loopStep = colIdx -> colIdx + 1;
                furtherThanTest = (first, second) -> first > second;
                rubbleDropSpeed = -1;
            }
            default -> throw new IllegalStateException("invalid player " + player);
        }
        var firstCollapsedColIdx = -1;
        var lastCollapsedColIdx = -1;
        for (var colIdx = startColIdx; collapseRangeCondition.test(colIdx); colIdx = loopStep.applyAsInt(colIdx)) {
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
                if (firstCollapsedColIdx < 0) {
                    firstCollapsedColIdx = colIdx;
                }
                lastCollapsedColIdx = colIdx;
                for (var rowIdx = 0; rowIdx < candidateRow.size(); rowIdx++) {
                    onRubbleRemoved.accept(candidateRow.set(rowIdx, null));
                }
            }
        }

        fill(collapsingRubbleClusterEndedRowIndexes, false);
        if (firstCollapsedColIdx >= 0) {
            for (var colIdx = loopStep.applyAsInt(firstCollapsedColIdx); movingRangeCondition.test(colIdx); colIdx = loopStep.applyAsInt(colIdx)) {
                var candidateColumn = rubble.get(colIdx);
                for (var rowIdx = 0; rowIdx < candidateColumn.size(); rowIdx++) {
                    var endOfCollapsingClusterOnThisRow = collapsingRubbleClusterEndedRowIndexes[rowIdx];
                    if (!endOfCollapsingClusterOnThisRow) {
                        var shape = candidateColumn.get(rowIdx);
                        if (shape != null) {
                            var newShape = Shape.builder().from(shape)
                                    .setHorizontalSpeed(rubbleDropSpeed)
                                    // drop no deeper than the first collapsed row
                                    .setInvisibleWallHorizontalOffset(max(0, min(firstCollapsedColIdx + rubbleDropSpeed, this.width - 1)))
                                    .setFallCausedBy(player)
                                    .build();
                            candidateColumn.set(rowIdx, newShape);
                            onRubbleAmended.accept(shape, newShape);
                        } else if (furtherThanTest.test(colIdx, lastCollapsedColIdx)) {
                            // we are now beyond last collapsed column, and we encountered an empty space: this row is now finished
                            collapsingRubbleClusterEndedRowIndexes[rowIdx] = true;
                        }
                    }
                }
            }
        }
    }

    public void rotatePlayersShape(Player player) {
        var shape = playerShapesByPlayer.get(player);
        if (shape != null) {
            var candidateShape = shape.rotate();
            if (!crossesSceneBoundary(candidateShape) && !overlapsWithAnotherPlayersShape(candidateShape, player) && !overlapsWithRubble(candidateShape)) {
                playerShapesByPlayer.put(player, candidateShape);
            }
        }
    }

    private boolean overlapsWithAnotherPlayersShape(Shape candidateShape, Player player) {
        for (var anotherPlayer : Player.ALL_PLAYERS) {
            if (anotherPlayer != player) {
                var anotherShape = playerShapesByPlayer.get(anotherPlayer);
                if (anotherShape != null) {
                    if (candidateShape.overlapsWith(anotherShape)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean crossesSceneBoundary(Shape candidateShape) {
        return candidateShape.horizontalOffset() < 0 || candidateShape.horizontalOffset() + candidateShape.pattern().height() > width
                || candidateShape.verticalOffset() < 0 || candidateShape.verticalOffset() + candidateShape.pattern().width() > height;
    }

    public enum ShapeLoweringResult {
        LOWERED, BECAME_RUBBLE, REACHED_BOTTOM
    }

    @FunctionalInterface
    interface IntBiPredicate {
        boolean test(int first, int second);
    }
}
