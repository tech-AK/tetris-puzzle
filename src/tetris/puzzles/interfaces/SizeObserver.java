package tetris.puzzles.interfaces;

/**
 * Provides an interface that data models that are responsible for holding the size change of tetrominoes should implement.
 */
public interface SizeObserver {

    /**
     * If this method is invoked, the class implementing this interface should
     * return the current size of a kachel for the tetrominoes as used from the various layout in the gamegrid.
     * @return The size of the squared kachel for the tetromino
     */
    int getTetrominoKachelSize();

    /**
     * If this method is invoked, the class implementing this interface should save the new kachel size that is used
     * by the parking spot panel.
     * @param totalGridLength The total grid length the parking spot panel has.
     * @param gridSize The grid size of the tetrominoes.
     */
    void setGridLengthParkingSpot(int totalGridLength, int gridSize);

    /**
     * If this method is invoked, the class implementing this interface should save the new kachel size that is used
     * by the shape panel.
     * @param totalGridLength The total grid length the shape panel has.
     * @param gridSize The grid size of the tetrominoes.
     */
    void setGridLengthShape(int totalGridLength, int gridSize);

    /**
     * If this method is invoked, the class implementing this interface should save the given instance of the {@link TetrominoObserver}.
     * If the size of a tetromino changes during the game, the implementing class should notify all {@link TetrominoObserver}'s that were added previously by this method.
     * @param tetrominoObserver Instance of the {@link TetrominoObserver} that should be notified about resizing of the tetromino's kachel.
     */
    void addObserver(TetrominoObserver tetrominoObserver);

}
