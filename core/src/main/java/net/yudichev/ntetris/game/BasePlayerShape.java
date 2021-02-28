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
        return PlayerShape.of(RectangularPattern.pattern(newRows), newHorizontalOffset, newVerticalOffset, horizontalSpeed());
    }

    public final void toSingleBlockShapes(Consumer<RubbleShape> shapeConsumer) {
        List<Row> rows = pattern().getRows();
        for (int horIdx = 0; horIdx < rows.size(); horIdx++) {
            Row row = rows.get(horIdx);
            for (int vertIdx = 0; vertIdx < row.width(); vertIdx++) {
                if (row.elementAt(vertIdx)) {
                    shapeConsumer.accept(RubbleShape.of(RectangularPattern.singleBlock(),
                            horizontalOffset() + horIdx,
                            verticalOffset() + vertIdx,
                            0));
                }
            }
        }
    }
}
