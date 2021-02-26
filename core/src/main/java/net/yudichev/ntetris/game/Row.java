package net.yudichev.ntetris.game;

import java.util.Arrays;

import static net.yudichev.ntetris.util.Preconditions.checkArgument;

final class Row {
    private final boolean[] elements;
    private final int startIndexInclusive;
    private final int endIndexExclusive;

    private Row(boolean[] elements, int startIndexInclusive, int endIndexExclusive) {
        checkArgument(startIndexInclusive >= 0);
        checkArgument(startIndexInclusive <= endIndexExclusive);
        checkArgument(endIndexExclusive <= elements.length, "endIndexExclusive %s > elements.length %s", endIndexExclusive, elements.length);
        this.elements = elements;
        this.startIndexInclusive = startIndexInclusive;
        this.endIndexExclusive = endIndexExclusive;
    }

    public static Row emptyRow(int width) {
        checkArgument(width >= 0);
        return row(new boolean[width]);
    }

    public static Row fullRow(int width) {
        boolean[] elements = new boolean[width];
        Arrays.fill(elements, true);
        return new Row(elements, 0, elements.length);
    }

    public static Row row(boolean... elements) {
        return new Row(elements, 0, elements.length);
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // mutable for GC/perf reasons
    public boolean[] getElements() {
        return elements;
    }

    public int width() {
        return endIndexExclusive - startIndexInclusive;
    }

    public boolean overlapsWith(Row anotherRow) {
        for (int i = 0; i < Math.min(width(), anotherRow.width()); i++) {
            if (elementAt(i) && anotherRow.elementAt(i)) {
                return true;
            }
        }
        return false;
    }

    public void impose(Row anotherRow) {
        for (int i = 0; i < anotherRow.width(); i++) {
            checkArgument(!(elementAt(i) && anotherRow.elementAt(i)), "cannot impose: elements would overlap");
            elements[i + startIndexInclusive] |= anotherRow.elementAt(i);
        }
    }

    public boolean isFull() {
        for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
            boolean element = elements[i];
            if (!element) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a live view into the part of the row.
     *
     * @param horizontalOffset offset in the row, zero-based
     * @param width            sub-row width
     * @return the sub-row
     */
    public Row subRow(int horizontalOffset, int width) {
        return new Row(elements, horizontalOffset + startIndexInclusive, horizontalOffset + startIndexInclusive + width);
    }

    public boolean elementAt(int offset) {
        return elements[offset + startIndexInclusive];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Row other = (Row) obj;
        return startIndexInclusive == other.startIndexInclusive && endIndexExclusive == other.endIndexExclusive && Arrays.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(elements);
        result = 31 * result + startIndexInclusive;
        result = 31 * result + endIndexExclusive;
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOfRange(elements, startIndexInclusive, endIndexExclusive));
    }
}
