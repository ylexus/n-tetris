package net.yudichev.ntetris.game;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

final class RectangularPattern {
    private final List<Row> rows;
    private final int width;

    /**
     * @param rows pattern rows, bottom row first
     */
    private RectangularPattern(List<Row> rows) {
        checkArgument(!rows.isEmpty());
        width = rows.stream()
                .reduce((row1, row2) -> {
                    checkArgument(row1.width() == row2.width());
                    return row1;
                })
                .get()
                .width();
        // We have to create a copy here as we must ensure that the contract enforced above (uniform row widths) always holds,
        // otherwise width() method will make no sense.
        // Note that this does not create a copy if source is already immutable.
        this.rows = ImmutableList.copyOf(rows);
    }

    public static RectangularPattern pattern(List<Row> rows) {
        return new RectangularPattern(rows);
    }

    public static RectangularPattern pattern(Row... rows) {
        return new RectangularPattern(ImmutableList.copyOf(rows));
    }

    public int width() {
        return width;
    }

    public int height() {
        return rows.size();
    }

    public List<Row> getRows() {
        return rows;
    }

    public boolean hasElementAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).elementAt(columnIndex);
    }

    @Override
    public String toString() {
        return rows.toString();
    }
}
