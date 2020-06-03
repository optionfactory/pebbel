package net.optionfactory.pebbel.parsing.ast;

/**
 * Token reference to source file.
 */
public class Source {

    public int row;
    public int col;
    public int endRow;
    public int endCol;

    public static Source of(int row, int col, int endRow, int endCol) {
        final Source src = new Source();
        src.row = row;
        src.col = col;
        src.endRow = endRow;
        src.endCol = endCol;
        return src;
    }

    @Override
    public String toString() {
        return String.format("%s:%s-%s:%s", row, col, endRow, endCol);
    }

}
