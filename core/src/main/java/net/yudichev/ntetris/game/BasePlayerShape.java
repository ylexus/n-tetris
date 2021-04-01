package net.yudichev.ntetris.game;

import net.yudichev.ntetris.PublicImmutablesStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.immutables.value.Value.Immutable;

@SuppressWarnings("ClassReferencesSubclass")
@Immutable
@PublicImmutablesStyle
abstract class BasePlayerShape extends Shape<PlayerShape> {

    public final PlayerShape rotate() {
        int newWidth = height();
        int newHeight = width();
        List<Row> newRows = new ArrayList<>(newHeight);
        for (int newY = 0; newY < newHeight; newY++) {
            newRows.add(Row.emptyRow(newWidth));
        }
        List<Row> oldRows = pattern().getRows();
        for (int newX = 0; newX < newWidth; newX++) {
            Row oldRow = oldRows.get(newX);
            for (int newY = 0; newY < newHeight; newY++) {
                newRows.get(newY).getElements()[newX] = oldRow.getElements()[newHeight - newY - 1];
            }
        }

        int newOffsetX = offsetX() + (newHeight - newWidth) / 2;
        int newOffsetY = offsetY() + (newWidth - newHeight) / 2;
        return PlayerShape.of(RectangularPattern.pattern(newRows), newOffsetX, newOffsetY, speedX());
    }

    public final void toSingleBlockShapes(Consumer<RubbleShape> shapeConsumer) {
        List<Row> rows = pattern().getRows();
        for (int patternRowIdx = 0; patternRowIdx < rows.size(); patternRowIdx++) {
            Row row = rows.get(patternRowIdx);
            for (int patternColIdx = 0; patternColIdx < row.width(); patternColIdx++) {
                if (row.elementAt(patternColIdx)) {
                    shapeConsumer.accept(RubbleShape.of(toAbsoluteX(patternColIdx), toAbsoluteY(patternRowIdx), 0));
                }
            }
        }
    }
}
