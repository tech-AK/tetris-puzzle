package tetris.puzzles.actions;

import tetris.puzzles.game.ParkingSpotHolder;
import tetris.puzzles.game.ShapeHolder;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This is an implementation of the {@link AbstractAction} class.
 * It provides instructions to execute if a user wants to select the next parking spot or shape.
 */
public class ActionNextSelect extends AbstractAction {

    int next;

    /**
     * Sets the parameters used for selecting the next shape.
     *
     * @param next relative number of next object from the current object
     *             E. g. {@code next = 1} would select the next right object to the currently selected object.
     */
    public ActionNextSelect(int next) {
        this.next = next;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof ShapeHolder) {
            ShapeHolder shapeHolder = (ShapeHolder) e.getSource();
            shapeHolder.selectNeighbourShape(next);

        } else if (e.getSource() instanceof ParkingSpotHolder) {
            ParkingSpotHolder parkingSpotHolder = (ParkingSpotHolder) e.getSource();
            parkingSpotHolder.selectNeighbourSlot(next);
        }
    }
}
