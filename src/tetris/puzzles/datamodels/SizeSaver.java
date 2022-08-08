package tetris.puzzles.datamodels;

import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.interfaces.TetrominoObserver;

import java.util.ArrayList;

/**
 * This class is used as helper during the layout initialization.
 * It saves the maximum length of a kachel in the ParkingSpot layout and shape layout and returns a maximum length that
 * fits into both layouts. Every layout with less space on the screen containing a grid for a tetromino should call at initialization
 * the methods {@link #setGridLengthParkingSpot(int, int)} respective {@link #setGridLengthShape(int, int)} to update the sizes.
 */
public class SizeSaver implements SizeObserver {

    private int gridLengthParkingSpot = -1;
    private int gridLengthShape = -1;

    private ArrayList<TetrominoObserver> tetrominoObservers;

    public SizeSaver() {
        tetrominoObservers = new ArrayList<>();
    }

    @Override
    public int getTetrominoKachelSize() {
        // Even if we have very much space (e. g. because user has chosen a small amount of shapes and parking spots),
        // never exceed a kachel length of 20, as tetrominoes then get just too big and it does not look good anymore.
        return Math.min(Math.min(gridLengthParkingSpot, gridLengthShape), 20);
    }

    @Override
    public void setGridLengthParkingSpot(int totalGridLength, int gridSize) {
        int newGridLengthParkingSpot = totalGridLength / gridSize;
        if (newGridLengthParkingSpot != gridLengthParkingSpot) {
            gridLengthParkingSpot = newGridLengthParkingSpot;
            notifyObservers();
        }
    }

    @Override
    public void setGridLengthShape(int totalGridLength, int gridSize) {
        int newGridLengthShape = totalGridLength / gridSize;
        if (newGridLengthShape != gridLengthShape) {
            gridLengthShape = newGridLengthShape;
            notifyObservers();
        }
    }


    /**
     * If this method is called, all previouly added {@link TetrominoObserver}s are notified about a size change in the kachel size of a tetromino.
     */
    private void notifyObservers() {
        for (TetrominoObserver observer : tetrominoObservers) {
            observer.onTetrominoSizeHasChanged();
        }
    }

    @Override
    public void addObserver(TetrominoObserver tetrominoObserver) {
        tetrominoObservers.add(tetrominoObserver);
    }

}
