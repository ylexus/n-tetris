package net.yudichev.ntetris.game;

import org.immutables.value.Value;

import java.util.List;

abstract class Shape<B extends Shape<B>> {

    @Value.Parameter
    public abstract RectangularPattern pattern();

    @Value.Parameter
    public abstract int horizontalOffset();

    @Value.Parameter
    public abstract int verticalOffset();

    @Value.Parameter
    public abstract int horizontalSpeed();

    public final B move() {
        return (B) withHorizontalOffset(horizontalOffset() + horizontalSpeed());
    }

    @SuppressWarnings("override")
    protected abstract B withHorizontalOffset(int horizontalOffset);

    public final boolean overlapsWith(B another) {
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

    public final boolean touchingVerticalEdge(int sceneWidth) {
        return horizontalOffset() == 0 || horizontalOffset() + pattern().height() == sceneWidth;
    }

    private boolean rowOverlaps(B anotherShape, int rowIdx) {
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
