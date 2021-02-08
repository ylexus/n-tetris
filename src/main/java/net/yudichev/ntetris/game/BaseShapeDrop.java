package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
@PublicImmutablesStyle
abstract
class BaseShapeDrop {
    @Value.Parameter
    public abstract RectangularPattern shape();

    @Value.Parameter
    public abstract int horizontalOffset();

    @Value.Parameter
    public abstract int verticalOffset();

    @Value.Parameter
    public abstract int horizontalSpeed();

    public final ShapeDrop move() {
        return ((ShapeDrop) this).withHorizontalOffset(horizontalOffset() + horizontalSpeed());
    }

    public final boolean overlapsWithRubble(ShapeDrop anotherDrop) {
        // more likely to overlap in the direction of our movement
        var rows = shape().getRows();
        if (horizontalSpeed() > 0) {
            for (int rowIdx = rows.size() - 1; rowIdx >= 0; rowIdx--) {
                if (rowOverlaps(anotherDrop, rowIdx)) {
                    return true;
                }
            }
        } else {
            for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
                if (rowOverlaps(anotherDrop, rowIdx)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasElementAtAbsoluteCoordinates(int absoluteRowIdx, int absoluteColIdx) {
        int relativeRowIdx = absoluteRowIdx - horizontalOffset();
        int relativeColIdx = absoluteColIdx - verticalOffset();
        if (relativeRowIdx < 0 || relativeRowIdx >= shape().height() || relativeColIdx < 0 || relativeColIdx >= shape().width()) {
            return false;
        }
        return shape().hasElementAt(relativeRowIdx, relativeColIdx);
    }

    public final boolean overlapsWithRubble(List<Row> rubble) {
        for (int i = 0; i < shape().height(); i++) {
            var rubbleColIdx = i + horizontalOffset();
            if (rubbleColIdx < rubble.size()) {
                Row row = rubble.get(rubbleColIdx);
                var relevantAbsoluteSubRow = row.subRow(
                        Math.min(verticalOffset(), row.width()),
                        Math.min(shape().width(), row.width() - verticalOffset()));
                if (relevantAbsoluteSubRow.overlapsWith(shape().getRows().get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public final void imposeOnRubble(List<Row> rubble) {
        for (int i = 0; i < shape().height(); i++) {
            Row row = rubble.get(i + horizontalOffset());
            var relevantAbsoluteSubRow = row.subRow(verticalOffset(), shape().width());
            relevantAbsoluteSubRow.impose(shape().getRows().get(i));
        }
    }

    private boolean rowOverlaps(ShapeDrop anotherDrop, int rowIdx) {
        var absoluteRowIdx = rowIdx + horizontalOffset();
        var candidateRow = shape().getRows().get(rowIdx);
        var elements = candidateRow.getElements();
        for (int colIdx = 0; colIdx < elements.length; colIdx++) {
            if (elements[colIdx]) {
                var absoluteColIdx = colIdx + verticalOffset();
                if (anotherDrop.hasElementAtAbsoluteCoordinates(absoluteRowIdx, absoluteColIdx)) {
                    return true;
                }
            }
        }
        return false;
    }
}
