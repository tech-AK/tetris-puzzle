package tetris.puzzles.datamodels;

/**
 * This class holds a data model for saving the position of a cell in a matrix grid.
 */
public class MatrixCoordinate {

    public int row = -1;
    public int column = -1;


    /**
     * Constructs a new MatrixCoordinate object that holds the position of a cell in a matrix grid.
     * @param row The row of the cell in a matrix grid
     * @param column The column of the cell in a matrix grid
     */
    public MatrixCoordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }


}
