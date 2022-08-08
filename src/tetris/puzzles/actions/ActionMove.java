package tetris.puzzles.actions;

import tetris.puzzles.game.GameGrid;
import tetris.puzzles.game.ParkingSpot;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;


/**
 * This is an implementation of the {@link AbstractAction} class.
 * It provides instructions to execute if a user wants to move the selected tetromino.
 */
public class ActionMove extends AbstractAction {

    int dx, dy;

    /**
     * Sets the parameters used for the moving.
     *
     * @param dx relative movement on x-axis
     * @param dy relative movement y-axis
     */
    public ActionMove(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TetrominoDraw tetromino;
        ArrayList<TetrominoDraw> tetrominoesInGrid;

        Object object = e.getSource();
        if (object instanceof GameGrid) {
            GameGrid gameGrid = (GameGrid) object;
            tetromino = gameGrid.getActiveTetromino();

            if (tetromino == null) {
                //no active tetromino to move
                return;
            }

            tetrominoesInGrid = gameGrid.getTetrominoObserver().getTetrominoesInGameGrid();

            int[] borders = gameGrid.getBorders();

            tetromino.translateInKachelUnitWithValidation(dx, dy, borders[0], borders[1], borders[2], borders[3], tetrominoesInGrid, true);

            gameGrid.repaint();

        } else if (object instanceof ParkingSpot) {
            ParkingSpot parkingSpot = (ParkingSpot) object;
            tetromino = parkingSpot.getTetrominoDraw();

            if (tetromino == null) {
                //no active tetromino to move
                return;
            }

            int[] borders = parkingSpot.getBorders();

            // In parking spots, there can only be one tetromino.
            // So we do not need to provide another list with other tetrominoes which cannot overlap.
            tetromino.translateInPxWithValidation(dx, dy, borders[0], borders[1], borders[2], borders[3], null, false);

            parkingSpot.repaint();
        }

    }
}


