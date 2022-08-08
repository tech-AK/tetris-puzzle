package tetris.puzzles.actions;

import tetris.puzzles.game.GameGrid;
import tetris.puzzles.game.ParkingSpot;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * This is an implementation of the {@link AbstractAction} class.
 * It provides instructions to execute if a user wants to turn the selected tetromino.
 */
public class ActionTurn extends AbstractAction {

    boolean turnRight;

    /**
     * Sets the parameters used for the turning.
     *
     * @param turnRight If true, the object is turned clockwise. Otherwise it is turned counter-clockwise.
     */
    public ActionTurn(boolean turnRight) {
        this.turnRight = turnRight;
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
                //no active tetromino to turn
                return;
            }

            tetrominoesInGrid = gameGrid.getTetrominoObserver().getTetrominoesInGameGrid();

            int[] borders = gameGrid.getBorders();

            tetromino.turn(turnRight, borders[0], borders[1], borders[2], borders[3], tetrominoesInGrid);
            gameGrid.repaint();

        } else if (object instanceof ParkingSpot) {
            ParkingSpot parkingSpot = (ParkingSpot) object;
            tetromino = parkingSpot.getTetrominoDraw();

            if (tetromino == null) {
                //no active tetromino to turn
                return;
            }

            int[] borders = parkingSpot.getBorders();

            // In parking spots, there can only be one tetromino.
            // So we do not need to provide another list with other tetrominoes which cannot overlap.
            tetromino.turn(turnRight, borders[0], borders[1], borders[2], borders[3], null);

            parkingSpot.repaint();
        }

    }
}
