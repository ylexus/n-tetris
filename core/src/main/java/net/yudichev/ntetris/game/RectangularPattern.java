package net.yudichev.ntetris.game;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static net.yudichev.ntetris.util.Preconditions.checkArgument;

final class RectangularPattern {
    private static final RectangularPattern SINGLE_BLOCK = pattern(Row.row(true));
    private final List<Row> rows;
    private final int width;

    /**
     * @param rows pattern rows, top row first
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
        this.rows = unmodifiableList(new ArrayList<>(rows));
    }

    public static RectangularPattern pattern(List<Row> rows) {
        return new RectangularPattern(rows);
    }

    public static RectangularPattern pattern(Row... rows) {
        return new RectangularPattern(asList(rows));
    }

    public static RectangularPattern singleBlock() {
        return SINGLE_BLOCK;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return rows.equals(((RectangularPattern) obj).rows);
    }

    @Override
    public int hashCode() {
        return rows.hashCode();
    }
}
