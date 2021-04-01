package net.yudichev.ntetris.game;

import net.yudichev.ntetris.canvas.GameCanvas;
import net.yudichev.ntetris.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static net.yudichev.ntetris.util.Preconditions.*;

/**
 * Coordinates in any scene are:
 * <pre>
 * -----> x (width, horizontal)
 * |
 * |
 * |
 * ↓
 * y (hight, vertical)
 * </pre>
 * <p>
 * Shapes are the same:
 * <pre>
 * -----> x - shape columns, column 0 is leftmost
 * |
 * |
 * |
 * ↓
 * y - shape rows, row 0 is highest
 * </pre>
 */
final class GameScene {
    private static final Logger logger = LoggerFactory.getLogger(GameScene.class);
    // leftmost row first, column indexes from top to bottom
    private final List<List<RubbleBlock>> rubbleRows;
    private final int width;
    private final int height;
    private final RubbleLifecycleListener rubbleLifecycleListener;
    private final Map<Player, PlayerShape> playerShapesByPlayer = new EnumMap<>(Player.class);
    private final Map<Player, PlayerShape> unmodifiablePlayerShapesByPlayer = Collections.unmodifiableMap(playerShapesByPlayer);
    private final int maxRubbleIterations;
    private final boolean[] collapsedCols;
    private double gameTime;

    GameScene(int width, int height, RubbleLifecycleListener rubbleLifecycleListener) {
        this.width = width;
        this.height = height;

        this.rubbleLifecycleListener = checkNotNull(rubbleLifecycleListener);
        rubbleRows = new ArrayList<>(height);
        for (int y = 0; y < height; y++) {
            List<RubbleBlock> row = new ArrayList<>(width);
            for (int x = 0; x < width; x++) {
                row.add(null);
            }
            rubbleRows.add(row);
        }
        maxRubbleIterations = width * height;
        collapsedCols = new boolean[width];
    }

    public void addRubbleColumnWithHole(int x, int holeIndex) {
        for (int y = 0; y < height; y++) {
            if (y != holeIndex) {
                RubbleShape shape = RubbleShape.of(x, y, 0);
                rubbleRows.get(y).set(x, new RubbleBlock(this, shape, gameTime));
            }
        }
    }

    public boolean attemptAddPlayerShape(Player player, PlayerShape shape) {
        if (overlapsWithRubble(shape)) {
            return false;
        }

        for (Player anotherPlayer : Player.ALL_PLAYERS) {
            PlayerShape anotherShape = playerShapesByPlayer.get(anotherPlayer);
            if (anotherShape != null && anotherShape != shape) {
                if (shape.overlapsWith(anotherShape)) {
                    return false;
                }
            }
        }
        checkArgument(playerShapesByPlayer.put(player, shape) == null, "player %s already has a shape", player);
        return true;
    }

    public void movePlayerShapeVertically(Player player, int offsetY) {
        PlayerShape playerShape = playerShapesByPlayer.get(player);
        if (playerShape != null) {
            int newOffsetY = playerShape.offsetY() + offsetY;
            if (newOffsetY >= 0 && newOffsetY + playerShape.height() <= height) {
                PlayerShape candidateShape = playerShape.withOffsetY(newOffsetY);
                if (!overlapsWithRubble(candidateShape)) {
                    for (Player anotherPlayer : Player.ALL_PLAYERS) {
                        if (anotherPlayer != player) {
                            PlayerShape anotherPlayersShape = playerShapesByPlayer.get(anotherPlayer);
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
        PlayerShape shape;
        while ((shape = playerShapesByPlayer.get(player)) != null) {
            ShapeLoweringResult shapeLoweringResult = lowerShape(player, shape);
            logger.debug("Player {} lower result {}", player, shapeLoweringResult);
            if (shapeLoweringResult == ShapeLoweringResult.LOWERED_WILL_BE_RUBBLE_SOON || shapeLoweringResult != ShapeLoweringResult.LOWERED) {
                return shapeLoweringResult;
            }
        }
        return null;
    }

    public Map<Player, PlayerShape> getPlayerShapesByPlayer() {
        return unmodifiablePlayerShapesByPlayer;
    }

    List<List<RubbleShape>> getRubbleRows() {
        return rubbleRows.stream()
                .map(row -> row.stream()
                        .map(rubbleBlock -> rubbleBlock == null ? null : rubbleBlock.getShape())
                        .collect(toList()))
                .collect(toList());
    }

    @Nullable
    public ShapeLoweringResult lowerShape(Player player) {
        PlayerShape shapeDrop = playerShapesByPlayer.get(player);
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

    /**
     * @return true if the shape moved
     */
    public boolean moveRubble(RubbleShape shape) {
        RubbleShape newShape = shape;
        if (shape.speedX() != 0) {
            int candidateX = shape.offsetX() + shape.speedX();
            if (shape.invisibleWallX() == candidateX) {
                // overlaps with invisible wall
                logger.debug("{} hit invisible wall {}, stopped", shape, candidateX);
                newShape = shape.stop();
            } else {
                RubbleBlock rubbleBlock = rubbleRows.get(shape.offsetY()).get(candidateX);
                if (rubbleBlock != null) {
                    RubbleShape overlappingRubble = rubbleBlock.getShape();
                    if (movingInSameDirection(shape, overlappingRubble)) {
                        logger.debug("{} moving in same dir as rubble {}, another calc needed", shape, overlappingRubble);
                    } else {
                        logger.debug("{} hit rubble {}, stopped", shape, overlappingRubble);
                        newShape = shape.stop();
                    }
                } else {
                    PlayerShape overlappingPlayerShape = playerShapeWithElementAt(candidateX, shape.offsetY(), shape.speedX());
                    if (overlappingPlayerShape != null) {
                        if (movingInSameDirection(shape, overlappingPlayerShape)) {
                            logger.debug("{} moving in same dir as player {}, another calc needed", shape, overlappingPlayerShape);
                        } else {
                            logger.debug("{} hit player {}, stopped", shape, overlappingPlayerShape);
                            newShape = shape.stop();
                        }
                    } else {
                        // move the shape
                        newShape = shape.withOffsetX(candidateX);
                        logger.debug("{} moved to {}", shape, newShape);
                    }
                }
            }
        }
        if (newShape != shape) {
            logger.debug("Clear1 {}", shape);
            RubbleBlock block = rubbleRows.get(shape.offsetY()).set(shape.offsetX(), null);
            checkState(block.getShape() == shape, "expected %s but was %s", shape, block);

            logger.debug("Set1 {}", newShape);
            block.transitionTo(newShape);
            checkState(rubbleRows.get(newShape.offsetY()).set(newShape.offsetX(), block) == null);

            int newShapePosition = newShape.offsetX();
            newShape.fallCausedBy().ifPresent(player -> maybeCollapseRubble(player, newShapePosition, 1));

            return true;
        }
        return false;
    }

    public void rotatePlayersShape(Player player) {
        PlayerShape shape = playerShapesByPlayer.get(player);
        if (shape != null) {
            PlayerShape candidateShape = shape.rotate();
            if (!crossesSceneBoundary(candidateShape) && !overlapsWithAnotherPlayersShape(candidateShape, player) && !overlapsWithRubble(candidateShape)) {
                playerShapesByPlayer.put(player, candidateShape);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // easier to read this way
    private static boolean movingInSameDirection(Shape<?> shape1, Shape<?> shape2) {
        return Integer.signum(shape1.speedX()) == Integer.signum(shape2.speedX());
    }

    @Nullable
    private PlayerShape playerShapeWithElementAt(int horizontalOffset, int verticalOffset, int friendlySpeed) {
        checkArgument(friendlySpeed != 0);
        for (PlayerShape playerShape : playerShapesByPlayer.values()) {
            if (playerShape.hasElementAtAbsoluteCoordinates(verticalOffset, horizontalOffset)) {
                return playerShape;
            }
        }
        return null;
    }

    private boolean overlapsWithRubble(Shape<?> shape) {
        for (int absY = shape.offsetY(); absY < shape.offsetY() + shape.height(); absY++) {
            List<RubbleBlock> rubbleRow = rubbleRows.get(absY);
            for (int absX = shape.offsetX(); absX < shape.offsetX() + shape.width(); absX++) {
                RubbleBlock rubbleBlock = rubbleRow.get(absX);
                if (rubbleBlock != null) {
                    RubbleShape rubbleShape = rubbleBlock.getShape();
                    // optimisation: knowing rubble shape is a single block, can be simple
                    if (rubbleShape != null && shape.pattern().hasElementAt(absX - shape.offsetX(), absY - shape.offsetY())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ShapeLoweringResult lowerShape(Player player, PlayerShape shape) {
        PlayerShape movedShape = shape.move();
        // are we touching another shape in the direction of our movement?
        for (Player anotherPlayer : Player.ALL_PLAYERS) {
            if (anotherPlayer != player) {
                PlayerShape anotherShape = playerShapesByPlayer.get(anotherPlayer);
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

            // check if we will be rubble on the next step
            PlayerShape shapeMovedTwice = movedShape.move();
            if (overlapsWithAnotherPlayersShape(shapeMovedTwice, player) || overlapsWithRubble(shapeMovedTwice)) {
                return ShapeLoweringResult.LOWERED_WILL_BE_RUBBLE_SOON;
            }

            return ShapeLoweringResult.LOWERED;
        }

        return ShapeLoweringResult.BECAME_RUBBLE;
    }

    private void convertToRubble(Player player, String reason) {
        PlayerShape playerShape = playerShapesByPlayer.remove(player);
        if (playerShape != null) {
            playerShape.toSingleBlockShapes(shape -> {
                logger.debug("Set2 {}", shape);
                RubbleBlock newRubbleBlock = new RubbleBlock(this, shape, gameTime);
                newRubbleBlock.onFrameStart(gameTime); // must initialise with current game time
                checkState(rubbleRows.get(shape.offsetY()).set(shape.offsetX(), newRubbleBlock) == null);
            });
            logger.debug("player {} shape {} converted to rubble because {}", player, playerShape, reason);
            maybeCollapseRubble(player, playerShape.offsetX(), playerShape.pattern().width());
        }
    }

    private void maybeCollapseRubble(Player player, int startingX, int width) {
        int startX;
        IntPredicate collapseRangeCondition;
        IntPredicate withinSceneCondition;
        IntUnaryOperator loopStep;
        IntBinaryOperator offsetDiff;
        int rubbleDropSpeed;
        switch (player) {
            case LEFT:
                startX = startingX + width - 1;
                collapseRangeCondition = x -> x >= startingX;
                withinSceneCondition = x -> x >= 0;
                loopStep = x -> x - 1;
                offsetDiff = Integer::sum;
                rubbleDropSpeed = 1;
                break;
            case RIGHT:
                startX = startingX;
                collapseRangeCondition = x -> x < startingX + width;
                withinSceneCondition = x -> x < this.width;
                loopStep = x -> x + 1;
                offsetDiff = (offset, diff) -> offset - diff;
                rubbleDropSpeed = -1;
                break;
            default:
                throw new IllegalStateException("invalid player " + player);
        }

        int firstCollapsedX = -1;
        Arrays.fill(collapsedCols, false);
        for (int x = startX; collapseRangeCondition.test(x); x = loopStep.applyAsInt(x)) {
            boolean columnFull = true;
            for (int y = 0; y < height; y++) {
                RubbleBlock block = rubbleRows.get(y).get(x);
                if (block == null || block.getShape().speedX() != 0) { // cannot collapse if the rubble is still moving
                    columnFull = false;
                    break;
                }
            }
            if (columnFull) {
                logger.debug("Collapsed rubble column {}", x);
                if (firstCollapsedX < 0) {
                    firstCollapsedX = x;
                }
                collapsedCols[x] = true;

                rubbleLifecycleListener.onRubbleColumnCollapsed(gameTime, x);
                for (int y = 0; y < height; y++) {
                    logger.debug("Clear2 at {}:{}", x, y);
                    rubbleRows.get(y).set(x, null);
                }
            }
        }

        // mark all the rubble beyond the fist collapsed column as falling, carefully choosing invisible wall
        if (firstCollapsedX >= 0) {
            for (int y = 0; y < height; y++) {
                List<RubbleBlock> row = rubbleRows.get(y);
                // starting from the column next to the first collapsed one until the edge of the scene
                for (int x = loopStep.applyAsInt(firstCollapsedX); withinSceneCondition.test(x); x = loopStep.applyAsInt(x)) {
                    RubbleBlock block = row.get(x);
                    if (block != null) {
                        RubbleShape oldShape = block.getShape();

                        int noOfRelevantCollapsedCols = 0;
                        for (int ix = firstCollapsedX; ix != x; ix = loopStep.applyAsInt(ix)) {
                            if (collapsedCols[ix]) {
                                noOfRelevantCollapsedCols++;
                            }
                        }

                        if (noOfRelevantCollapsedCols > 0) {
                            RubbleShape newShape = RubbleShape.builder().from(oldShape)
                                    .setSpeedX(rubbleDropSpeed)
                                    // drop no deeper than the first collapsed column
                                    .setInvisibleWallX(max(0, min(offsetDiff.applyAsInt(oldShape.offsetX(), noOfRelevantCollapsedCols + 1), this.width - 1)))
                                    .setFallCausedBy(player)
                                    .build();
                            if (!newShape.equals(oldShape)) {
                                block.transitionTo(newShape);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean overlapsWithAnotherPlayersShape(PlayerShape candidateShape, Player player) {
        for (Player anotherPlayer : Player.ALL_PLAYERS) {
            if (anotherPlayer != player) {
                PlayerShape anotherShape = playerShapesByPlayer.get(anotherPlayer);
                if (anotherShape != null) {
                    if (candidateShape.overlapsWith(anotherShape)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean crossesSceneBoundary(Shape<?> shape) {
        return shape.offsetX() < 0 || shape.offsetX() + shape.width() > width
                || shape.offsetY() < 0 || shape.offsetY() + shape.height() > height;
    }

    @SuppressWarnings("DefaultLocale")
    public String prettyPrintRubble() {
        StringBuilder sb = new StringBuilder(5 * width * height + 2 * height);
        for (int y = 0; y < height; y++) {
            sb.append("\n");
            List<RubbleBlock> row = rubbleRows.get(y);
            for (int x = 0; x < width; x++) {
                RubbleBlock block = row.get(x);
                if (block == null) {
                    sb.append("[   ]");
                } else {
                    RubbleShape shape = block.getShape();
                    if (shape.speedX() == 0) {
                        sb.append("[ O ]");
                    } else {
                        sb.append('[').append(shape.speedX() < 0 ? '<' : '>').append(String.format("%02d", shape.invisibleWallX())).append(']');
                    }
                }
            }
        }
        playerShapesByPlayer.forEach((player, playerShape) -> sb.append("\n").append(player).append('=').append(playerShape));
        return sb.toString();
    }

    public void initialiseRubbleFromPrettyPrint(double gameTime, String prettyPrint) {
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        String[] rows = prettyPrint.split("\\n");
        for (int y = 0; y < height; y++) {
            String row = rows[y];
            for (int x = 0; x < width; x++) {
                String encodedBlock = row.substring(x * 5 + 1, x * 5 + 4);
                RubbleShape shape = null;
                if (" O ".equals(encodedBlock)) {
                    shape = RubbleShape.of(x, y, 0);
                } else if (!"   ".equals(encodedBlock)) {
                    int speedX = encodedBlock.charAt(0) == '<' ? -1 : 1;
                    shape = RubbleShape.builder()
                            .setOffsetX(x)
                            .setOffsetY(y)
                            .setSpeedX(speedX)
                            .setFallCausedBy(speedX == -1 ? Player.RIGHT : Player.LEFT)
                            .setInvisibleWallX(Integer.parseInt(encodedBlock.substring(1)))
                            .build();
                }
                if (shape != null) {
                    rubbleRows.get(y).set(x, new RubbleBlock(this, shape, gameTime));
                }
            }
        }
    }

    public void onFrameStart(double gameTime) {
        this.gameTime = gameTime;
        rubbleRows.forEach(row -> row.forEach(block -> {
            if (block != null) {
                block.onFrameStart(gameTime);
            }
        }));
    }

    /**
     * @return true if failed to resolve the move a number of iterations
     */
    public boolean moveRubble() {
        boolean moved;
        int n = 0;
        do {
            logger.debug("{}: Iteration {}", gameTime, n);
            moved = false;
            for (int y = 0; y < rubbleRows.size(); y++) {
                List<RubbleBlock> row = rubbleRows.get(y);
                for (int x = 0; x < row.size(); x++) {
                    RubbleBlock rubbleBlock = row.get(x);
                    if (rubbleBlock != null) {
                        moved |= rubbleBlock.move();
                    }
                }
            }
            if (moved) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{}", prettyPrintRubble());
                }
            }
        } while (moved && n++ < maxRubbleIterations);

        return moved;
    }

    public void render(GameCanvas canvas) {
        rubbleRows.forEach(row -> row.forEach(block -> {
            if (block != null) {
                block.render(canvas);
            }
        }));
    }

    public enum ShapeLoweringResult {
        LOWERED, LOWERED_WILL_BE_RUBBLE_SOON, BECAME_RUBBLE, REACHED_BOTTOM
    }
}
