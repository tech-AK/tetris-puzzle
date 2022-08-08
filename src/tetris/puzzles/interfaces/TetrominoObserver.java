package tetris.puzzles.interfaces;

import tetris.puzzles.tetromino.TetrominoDraw;

import java.util.ArrayList;

/**
 * An interface that provides lots of functions that concern the tetrominoes in the whole game.
 */
public interface TetrominoObserver {

    /**
     * Notifies the TetrominoObserver that a new Tetromino was selected.
     * @param tetrominoDraw The {@link TetrominoDraw} object that was selected.
     * @param setByGameGrid If ture, the game grid has called this method. If false, the parking spot has called this method.
     */
    void setSelectedTetromino(TetrominoDraw tetrominoDraw, boolean setByGameGrid);

    /**
     * Returns the selected {@link TetrominoDraw}.
     * @return Selected {@link TetrominoDraw}.
     */
    TetrominoDraw getSelectedTetromino();

    /**
     * Returns a ArrayList containing every {@link TetrominoDraw} that is shown in grid.
     * Note that the implementing class should return a COPY of the full arraylist in order to prevent ConcurrentModifactionExceptions.
     * @return Selected {@link TetrominoDraw}.
     */
    ArrayList<TetrominoDraw> getTetrominoesInGameGrid();

    /**
     * Returns a {@link TetrominoDraw} at the specified index or null if index is not available.
     * @param index The index to be returned
     * @return {@link TetrominoDraw} at index
     */
    TetrominoDraw getTetrominoesInGameGridAtIndex(int index); //we do not return the whole list as then ConcurrentModificationException could occur

    /**
     * Adds a new Tetromino the the game grid list.
     * @param tetrominoDraw  {@link TetrominoDraw} to be aded
     */
    void addTetrominoToGameGridList(TetrominoDraw tetrominoDraw);

    /**
     * Removes a Tetromino from the game grid and the parking spots.
     * @param tetrominoDraw  {@link TetrominoDraw} to be removed.
     */
    void removeTetromino(TetrominoDraw tetrominoDraw);

    /**
     * Deselects all tetrominoes in the parking spots.
     */
    void deselectAllTetrominoesInParkingSpot();

    /**
     * Implementing class should initialize a game over screen.
     */
    void onGameOver();

    /**
     * Implementing class should disable the game over check. This method can be used to prevent false-positive game overs if panel is resized.
     */
    void disableGameOver();

    /**
     * Implementing class should enable the game over check back.
     */
    void enableGameOver();

    /**
     * Implementing class should trigger the re-sizing of every component due to the size change of tetrominoes.
     */
    void onTetrominoSizeHasChanged();
}
