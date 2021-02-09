package net.yudichev.ntetris.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.yudichev.ntetris.game.Row.emptyRow;

final class Scene {
    private static final Logger logger = LoggerFactory.getLogger(Scene.class);
    // leftmost row first, column indexes from top to bottom
    private final List<Row> rubble;
    private final int width;
    private final int height;
    private final Map<Player, ShapeDrop> shapeDropsByPlayer = new HashMap<>(2);

    public Scene(int width, int height) {
        rubble = new ArrayList<>(width);
        this.width = width;
        this.height = height;
        for (int i = 0; i < width; i++) {
            //noinspection SuspiciousNameCombination
            rubble.add(emptyRow(height));
        }
    }

    public boolean attemptAddPlayerShape(Player player, ShapeDrop shapeDrop) {
        if (shapeDrop.overlapsWithRubble(rubble)) {
            return false;
        }
        for (Player anotherPlayer : Player.ALL_PLAYERS) {
            ShapeDrop anotherDrop = shapeDropsByPlayer.get(anotherPlayer);
            if (anotherDrop != null && anotherDrop != shapeDrop) {
                if (shapeDrop.overlapsWithAnotherDrop(anotherDrop)) {
                    return false;
                }
            }
        }
        checkArgument(shapeDropsByPlayer.put(player, shapeDrop) == null, "player %s already has a shape", player);
        return true;
    }

    public void movePlayerShapeVertically(Player player, int verticalOffset) {
        var shapeDrop = shapeDropsByPlayer.get(player);
        if (shapeDrop != null) {
            var newVerticalOffset = shapeDrop.verticalOffset() + verticalOffset;
            if (newVerticalOffset >= 0 && newVerticalOffset + shapeDrop.shape().width() <= height) {
                var candidateShapeDrop = shapeDrop.withVerticalOffset(newVerticalOffset);
                if (!candidateShapeDrop.overlapsWithRubble(rubble)) {
                    for (Player anotherPlayer : Player.ALL_PLAYERS) {
                        if (anotherPlayer != player) {
                            var anotherPlayersDrop = shapeDropsByPlayer.get(anotherPlayer);
                            if (anotherPlayersDrop != null && candidateShapeDrop.overlapsWithAnotherDrop(anotherPlayersDrop)) {
                                convertToRubble(player);
                                convertToRubble(anotherPlayer);
                                return;
                            }
                        }
                    }
                    shapeDropsByPlayer.put(player, candidateShapeDrop);
                }
            }
        }
    }

    @Nullable
    public ShapeLoweringResult dropShape(Player player) {
        var shapeDrop = shapeDropsByPlayer.get(player);
        if (shapeDrop != null) {
            var shapeLoweringResult = ShapeLoweringResult.BECAME_RUBBLE;
            while ((shapeDrop = shapeDropsByPlayer.get(player)) != null) {
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

    public Map<Player, ShapeDrop> getShapeDropsByPlayer() {
        return shapeDropsByPlayer;
    }

    public void addRubble(int horizontalOffsetFromLeft, Row row) {
        checkArgument(row.width() == height);
        rubble.set(horizontalOffsetFromLeft, row);
    }

    public List<Row> getRubble() {
        return rubble;
    }

    @Nullable
    public ShapeLoweringResult lowerShape(Player player) {
        var shapeDrop = shapeDropsByPlayer.get(player);
        return shapeDrop == null ? null : lowerShape(player, shapeDrop);
    }

    public void deletePlayerShapes() {
        shapeDropsByPlayer.clear();
    }

    private ShapeLoweringResult lowerShape(Player player, ShapeDrop shapeDrop) {
        var movedShapeDrop = shapeDrop.move();
        // are we touching another shape in the direction of our movement?
        for (Player anotherPlayer : Player.ALL_PLAYERS) {
            ShapeDrop anotherDrop = shapeDropsByPlayer.get(anotherPlayer);
            if (anotherDrop != null && anotherDrop != shapeDrop) {
                if (movedShapeDrop.overlapsWithAnotherDrop(anotherDrop)) {
                    convertToRubble(player);
                    convertToRubble(anotherPlayer);
                }
            }
        }

        // if we are still not rubble, check if we are touching rubble
        if (shapeDropsByPlayer.containsKey(player)) {
            if (movedShapeDrop.overlapsWithRubble(rubble)) {
                convertToRubble(player);
            }
        }

        // if we are still not rubble, check if we reached the edge
        if (shapeDropsByPlayer.containsKey(player)) {
            if (movedShapeDrop.touchingEdge(width)) {
                shapeDropsByPlayer.remove(player);
                return ShapeLoweringResult.REACHED_BOTTOM;
            }
            shapeDropsByPlayer.put(player, movedShapeDrop);
            return ShapeLoweringResult.LOWERED;
        }
        return ShapeLoweringResult.BECAME_RUBBLE;
    }

    private void convertToRubble(Player player) {
        var shapeDrop = shapeDropsByPlayer.remove(player);
        if (shapeDrop != null) {
            shapeDrop.imposeOnRubble(rubble);
            maybeCollapseRubble(player, shapeDrop.horizontalOffset(), shapeDrop.shape().height());
        }
    }

    private void maybeCollapseRubble(Player player, int offset, int width) {
        for (int i = offset; i < offset + width; i++) {
            if (rubble.get(i).isFull()) {
                switch (player) {
//                    case LEFT -> rubble.remo(i)
                }
            }
        }
    }

    public enum ShapeLoweringResult {
        LOWERED, BECAME_RUBBLE, REACHED_BOTTOM
    }
}
