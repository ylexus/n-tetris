package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

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

    public final Shape move() {
        var shape = (Shape) this;
        return shape.withHorizontalOffset(shape.horizontalOffset() + horizontalSpeed());
    }

    public final boolean overlapsWith(Shape another) {
        // more likely to overlap in the direction of our movement
        var rows = pattern().getRows();
        if (horizontalSpeed() > 0) {
            for (var rowIdx = rows.size() - 1; rowIdx >= 0; rowIdx--) {
                if (rowOverlaps(another, rowIdx)) {
                    return true;
                }
            }
        } else {
            for (var rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
                if (rowOverlaps(another, rowIdx)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasElementAtAbsoluteCoordinates(int absoluteRowIdx, int absoluteColIdx) {
        var relativeRowIdx = absoluteRowIdx - horizontalOffset();
        var relativeColIdx = absoluteColIdx - verticalOffset();
        if (relativeRowIdx < 0 || relativeRowIdx >= pattern().height() || relativeColIdx < 0 || relativeColIdx >= pattern().width()) {
            return false;
        }
        return pattern().hasElementAt(relativeRowIdx, relativeColIdx);
    }

    public final void toSingleBlockShapes(Consumer<Shape> shapeConsumer) {
        var rows = pattern().getRows();
        for (var horIdx = 0; horIdx < rows.size(); horIdx++) {
            var row = rows.get(horIdx);
            for (var vertIdx = 0; vertIdx < row.width(); vertIdx++) {
                if (row.elementAt(vertIdx)) {
                    shapeConsumer.accept(Shape.of(RectangularPattern.singleBlock(),
                            horizontalOffset() + horIdx,
                            verticalOffset() + vertIdx,
                            0));
                }
            }
        }
    }

    public final boolean touchingEdge(int sceneWidth) {
        return horizontalOffset() == 0 || horizontalOffset() + pattern().height() == sceneWidth;
    }

    private boolean rowOverlaps(Shape anotherShape, int rowIdx) {
        var absoluteRowIdx = rowIdx + horizontalOffset();
        var candidateRow = pattern().getRows().get(rowIdx);
        var elements = candidateRow.getElements();
        for (var colIdx = 0; colIdx < elements.length; colIdx++) {
            if (elements[colIdx]) {
                var absoluteColIdx = colIdx + verticalOffset();
                if (anotherShape.hasElementAtAbsoluteCoordinates(absoluteRowIdx, absoluteColIdx)) {
                    return true;
                }
            }
        }
        return false;
    }
}
