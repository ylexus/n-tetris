package net.yudichev.ntetris.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.ntetris.game.Row.emptyRow;

final class Scene {
    // leftmost row first, column indexes from top to bottom
    private final List<Row> rubble;
    private final int height;
    private final Map<Player, ShapeDrop> shapeDropsByPlayer = new HashMap<>(2);

    public Scene(int width, int height) {
        rubble = new ArrayList<>(width);
        this.height = height;
        for (int i = 0; i < width; i++) {
            //noinspection SuspiciousNameCombination
            rubble.add(emptyRow(height));
        }
    }

    public void addPlayerShape(Player player, ShapeDrop shapeDrop) {
        checkArgument(shapeDropsByPlayer.put(player, shapeDrop) == null, "player %s already has a shape", player);
    }

    public void movePlayerShapeVertically(Player player, int verticalOffset) {
        var shapeDrop = checkNotNull(shapeDropsByPlayer.get(player), "player %s has no shape", player);
        var newVerticalOffset = shapeDrop.verticalOffset() + verticalOffset;
        if (newVerticalOffset >= 0 && newVerticalOffset + shapeDrop.shape().width() <= height) {

            // TODO check for rubble

            shapeDropsByPlayer.put(player, shapeDrop.withVerticalOffset(newVerticalOffset));
        }
    }

    public void fallShapes() {
        // TODO check for reaching the other side

        for (Player player : Player.ALL_PLAYERS) {
            var shapeDrop = shapeDropsByPlayer.get(player);
            if (shapeDrop != null) {
                var movedShapeDrop = shapeDrop.move();
                // are we touching another shape in the direction of our movement?
                for (Player anotherPlayer : Player.ALL_PLAYERS) {
                    ShapeDrop anotherDrop = shapeDropsByPlayer.get(anotherPlayer);
                    if (anotherDrop != null && anotherDrop != shapeDrop) {
                        if (movedShapeDrop.overlapsWithRubble(anotherDrop)) {
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

                // if we are still not rubble, move
                if (shapeDropsByPlayer.containsKey(player)) {
                    shapeDropsByPlayer.put(player, movedShapeDrop);
                }
            }
        }
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

    private void convertToRubble(Player player) {
        var shapeDrop = shapeDropsByPlayer.remove(player);
        if (shapeDrop != null) {
            shapeDrop.imposeOnRubble(rubble);
        }
    }
}
