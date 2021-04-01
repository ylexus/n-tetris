package net.yudichev.ntetris.game;

import org.immutables.value.Value;

import java.util.List;

abstract class Shape<B extends Shape<B>> {

    @Value.Parameter
    public abstract RectangularPattern pattern();

    @Value.Parameter
    public abstract int offsetX();

    @Value.Parameter
    public abstract int offsetY();

    @Value.Parameter
    public abstract int speedX();

    public final int width() {
        return pattern().width();
    }

    public final int height() {
        return pattern().height();
    }

    public final B move() {
        return (B) withOffsetX(offsetX() + speedX());
    }

    @SuppressWarnings("override")
    protected abstract B withOffsetX(int horizontalOffset);

    public final boolean overlapsWith(B another) {
        // more likely to overlap in the direction of our movement
        List<Row> rows = pattern().getRows();
        if (speedX() > 0) {
            for (int patternY = rows.size() - 1; patternY >= 0; patternY--) {
                if (rowOverlaps(another, patternY)) {
                    return true;
                }
            }
        } else {
            for (int patternY = 0; patternY < rows.size(); patternY++) {
                if (rowOverlaps(another, patternY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasElementAtAbsoluteCoordinates(int absoluteY, int absoluteX) {
        int patternY = absoluteY - offsetY();
        int patternX = absoluteX - offsetX();
        if (patternY < 0 || patternY >= height() || patternX < 0 || patternX >= width()) {
            return false;
        }
        return pattern().hasElementAt(patternX, patternY);
    }

    public final boolean touchingVerticalEdge(int sceneWidth) {
        return offsetX() == 0 || offsetX() + width() == sceneWidth;
    }

    public int toAbsoluteX(int patternX) {
        return offsetX() + patternX;
    }

    public int toAbsoluteY(int patternY) {
        return offsetY() + patternY;
    }

    private boolean rowOverlaps(B anotherShape, int patternY) {
        Row candidateRow = pattern().getRows().get(patternY);
        int absoluteY = toAbsoluteY(patternY);
        boolean[] elements = candidateRow.getElements();
        for (int patternX = 0; patternX < elements.length; patternX++) {
            if (elements[patternX]) {
                int absoluteX = toAbsoluteX(patternX);
                if (anotherShape.hasElementAtAbsoluteCoordinates(absoluteY, absoluteX)) {
                    return true;
                }
            }
        }
        return false;
    }
}
