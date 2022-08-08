package tetris.puzzles.tetromino;

import tetris.tools.Tetromino;

/**
 * This class extends {@link Tetromino} and provides more functionality needed by the game Puzzles.
 * This class is responsible for creating and manipulating the internal array of the tetrominoes
 */
public class TetrominoArray extends Tetromino {

    int[][] internalArray; //we use another name for the array of the stone

    public TetrominoArray(int[][] internalArray) {
        super(internalArray.length);
        super.stein = internalArray;
        this.internalArray = internalArray;
    }

    public TetrominoArray(Tetromino spielstein) {
        super(spielstein.stein.length);
        super.stein = spielstein.stein;
        this.internalArray = super.stein;
    }

    /**
     * Moves the tetromino array to the minimal embedding.
     */
    public void moveToMinimalEmbedding() {
        super.moveTop();
        super.moveLeftSide();
    }

    public void turnLeft() {
        Tetromino turnedStein = super.turn().turn().turn();
        super.stein = turnedStein.stein; //update our array by using the new returned array
        internalArray = stein; //update our reflection
    }

    public void turnRight() {
        Tetromino turnedStein = super.turn();
        super.stein = turnedStein.stein; //update our array by using the new returned array
        internalArray = stein; //update our reflection
    }

    public void mirrorHorizontally() {
        Tetromino mirroredSpielstein = super.mirror2();
        super.stein = mirroredSpielstein.stein;
        internalArray = stein;
    }

    public void mirrorVertically() {
        Tetromino mirroredSpielstein = super.mirror();
        super.stein = mirroredSpielstein.stein;
        internalArray = stein;
    }

    /**
     * Returns the index of the first column in the first row holding an active kachel.
     * Note: If Tetromino is not in his standard embedding and thus, the first row might not contain any kachel, this method may return -1.
     *
     * @return The first column index. Returns -1 if no active kachel was found in the first row.
     */
    public int getRowIndexOfFirstActiveKachel() {
        for (int i = 0; i < internalArray.length; i++) {
            if (internalArray[0][i] > 0) {
                return i;
            }
        }
        return -1;
    }

    public int[][] getInternalArray() {
        return internalArray;
    }

    public int getInternalArrayLength() {
        return internalArray.length;
    }
}
