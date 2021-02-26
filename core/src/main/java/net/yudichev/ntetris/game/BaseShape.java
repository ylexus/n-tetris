package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@PublicImmutablesStyle
abstract class BaseShape {
    @Value.Parameter
    public abstract RectangularPattern pattern();

    @Value.Parameter
    public abstract int horizontalOffset();

    @Value.Parameter
    public abstract int verticalOffset();

    @Value.Parameter
    public abstract int horizontalSpeed();

    @Value.Default
    public int invisibleWallHorizontalOffset() {
        return -1;
    }

    public abstract Optional<Player> fallCausedBy();

    public Shape stopFalling() {
        return Shape.builder()
                .from(this)
                .setHorizontalSpeed(0)
                .setInvisibleWallHorizontalOffset(-1)
                .setFallCausedBy(Optional.empty())
                .build();
    }

    public final Shape move() {
        Shape shape = (Shape) this;
        return shape.withHorizontalOffset(shape.horizontalOffset() + horizontalSpeed());
    }

    public final boolean overlapsWith(Shape another) {
        // more likely to overlap in the direction of our movement
        List<Row> rows = pattern().getRows();
        if (horizontalSpeed() > 0) {
            for (int rowIdx = rows.size() - 1; rowIdx >= 0; rowIdx--) {
                if (rowOverlaps(another, rowIdx)) {
                    return true;
                }
            }
        } else {
            for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
                if (rowOverlaps(another, rowIdx)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasElementAtAbsoluteCoordinates(int absoluteRowIdx, int absoluteColIdx) {
        int relativeRowIdx = absoluteRowIdx - horizontalOffset();
        int relativeColIdx = absoluteColIdx - verticalOffset();
        if (relativeRowIdx < 0 || relativeRowIdx >= pattern().height() || relativeColIdx < 0 || relativeColIdx >= pattern().width()) {
            return false;
        }
        return pattern().hasElementAt(relativeRowIdx, relativeColIdx);
    }

    public final void toSingleBlockShapes(Consumer<Shape> shapeConsumer) {
        List<Row> rows = pattern().getRows();
        for (int horIdx = 0; horIdx < rows.size(); horIdx++) {
            Row row = rows.get(horIdx);
            for (int vertIdx = 0; vertIdx < row.width(); vertIdx++) {
                if (row.elementAt(vertIdx)) {
                    shapeConsumer.accept(Shape.of(RectangularPattern.singleBlock(),
                            horizontalOffset() + horIdx,
                            verticalOffset() + vertIdx,
                            0));
                }
            }
        }
    }

    public final boolean touchingVerticalEdge(int sceneWidth) {
        return horizontalOffset() == 0 || horizontalOffset() + pattern().height() == sceneWidth;
    }

    public final Shape rotate() {
        int newWidth = pattern().height();
        int newHeight = pattern().width();
        List<Row> newRows = new ArrayList<>(pattern().width());
        for (int newRowIdx = 0; newRowIdx < pattern().width(); newRowIdx++) {
            newRows.add(Row.emptyRow(pattern().height()));
        }
        List<Row> oldRows = pattern().getRows();
        for (int newColIdx = 0; newColIdx < oldRows.size(); newColIdx++) {
            Row oldRow = oldRows.get(newColIdx);
            for (int newRowIdx = 0; newRowIdx < pattern().width(); newRowIdx++) {
                newRows.get(newRowIdx).getElements()[oldRows.size() - newColIdx - 1] = oldRow.getElements()[newRowIdx];
            }
        }

        int newHorizontalOffset = horizontalOffset() + (pattern().height() - newHeight) / 2;
        int newVerticalOffset = verticalOffset() + (pattern().width() - newWidth) / 2;
        Shape result = Shape.of(RectangularPattern.pattern(newRows), newHorizontalOffset, newVerticalOffset, horizontalSpeed());
        if (invisibleWallHorizontalOffset() >= 0) {
            result = result.withInvisibleWallHorizontalOffset(invisibleWallHorizontalOffset());
        }
        return result;
    }

    private boolean rowOverlaps(Shape anotherShape, int rowIdx) {
        int absoluteRowIdx = rowIdx + horizontalOffset();
        Row candidateRow = pattern().getRows().get(rowIdx);
        boolean[] elements = candidateRow.getElements();
        for (int colIdx = 0; colIdx < elements.length; colIdx++) {
            if (elements[colIdx]) {
                int absoluteColIdx = colIdx + verticalOffset();
                if (anotherShape.hasElementAtAbsoluteCoordinates(absoluteRowIdx, absoluteColIdx)) {
                    return true;
                }
            }
        }
        return false;
    }
}
