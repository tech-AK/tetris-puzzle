package tetris.puzzles.game;

import tetris.puzzles.actions.ActionNextSelect;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.ControlInterface;
import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.interfaces.TetrominoObserver;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static tetris.puzzles.game.ShapeHolder.BORDER_STROKE;

/**
 * This class is responsible for holding each parking spot.
 */
public class ParkingSpotHolder extends JPanel {

    public static final int MARGIN_BETWEEN_PARKING_SPOTS = 10;
    UserPreferences userPreferences;
    ParkingSpot[] parkingSpots;
    ParkingSpot selectedSpot;

    SizeObserver sizeObserver;
    TetrominoObserver tetrominoObserver;
    ControlInterface controlInterface;

    /**
     * Constructs a new ParkingSpotHolder.
     * @param tetrominoObserver A {@link TetrominoObserver} that can be used to get selected tetrominoes
     * @param controlInterface A {@link ControlInterface} that can be used to display a warning to the user on forbidden operations.
     * @param sizeObserver A {@link SizeObserver} that can be used to get the current size of tetrominoes
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     */
    ParkingSpotHolder(TetrominoObserver tetrominoObserver, ControlInterface controlInterface, SizeObserver sizeObserver, UserPreferences userPreferences) {
        this.tetrominoObserver = tetrominoObserver;
        this.sizeObserver = sizeObserver;
        this.controlInterface = controlInterface;
        this.userPreferences = userPreferences;

        setBackground(Color.WHITE);

        addParkingSpots();

        addKeyBindings();

        addKeyListener();
    }

    /**
     * Adds Key Listener.
     */
    private void addKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);

                int pressedNumber = Character.getNumericValue(e.getKeyChar());
                if (pressedNumber > 0 && pressedNumber <= userPreferences.getNumberOfParkingSpots()) {

                    selectParkingSpotByNumber(pressedNumber);

                } else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    tryToFillParkingSpot();
                }

            }
        });
    }

    /**
     * Selects the parking spot with the corresponding number.
     * @param pressedNumber The number that was pressed
     */
    private void selectParkingSpotByNumber(int pressedNumber) {
        if (selectedSpot != null) {
            selectedSpot.deselect();
        }

        selectedSpot = parkingSpots[pressedNumber - 1];
        selectedSpot.select();
    }

    /**
     * Tries to fill the parking spot by the currently selected tetromino.
     */
    private void tryToFillParkingSpot() {
        if (selectedSpot != null) {
            TetrominoDraw selectedTetromino = tetrominoObserver.getSelectedTetromino();

            if (selectedTetromino != null) {
                boolean tetrominoFits = selectedSpot.insertNewTetromino(selectedTetromino);
                if (tetrominoFits) {
                    tetrominoObserver.removeTetromino(selectedTetromino);
                } else {
                    controlInterface.displayWarning();
                }
            }
        }
    }

    /**
     * Adds new parking spots to the parking spot holder.
     */
    private void addParkingSpots() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        parkingSpots = new ParkingSpot[userPreferences.getNumberOfParkingSpots()];

        GridBagConstraints gbc_parking_slot_in_between = new GridBagConstraints();
        gbc_parking_slot_in_between.insets = new Insets(BORDER_STROKE, 0, BORDER_STROKE, MARGIN_BETWEEN_PARKING_SPOTS);

        GridBagConstraints gbc_first_parking_slot = new GridBagConstraints();
        gbc_first_parking_slot.insets = new Insets(BORDER_STROKE, MARGIN_BETWEEN_PARKING_SPOTS / 2, BORDER_STROKE, MARGIN_BETWEEN_PARKING_SPOTS);

        GridBagConstraints gbc_last_parking_slot = new GridBagConstraints();
        gbc_last_parking_slot.insets = new Insets(BORDER_STROKE, 0, BORDER_STROKE, MARGIN_BETWEEN_PARKING_SPOTS / 2);


        for (int i = 0; i < parkingSpots.length; i++) {
            parkingSpots[i] = new ParkingSpot(tetrominoObserver, sizeObserver, userPreferences, i + 1);
            if (i == 0) {
                add(parkingSpots[i], gbc_first_parking_slot);
            } else if (i == parkingSpots.length - 1) {
                add(parkingSpots[i], gbc_last_parking_slot);
            } else {
                add(parkingSpots[i], gbc_parking_slot_in_between);
            }
        }
    }

    /**
     * Adds Key Bindings (additionally to the key listener as otherwise the key bindings of the game grid would consume our keys even if this component is focused).
     */
    private void addKeyBindings() {
        //override the GameGrid consumption of left/right arrows if this component is focused
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "select_left_spot");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "select_right_spot");

        getActionMap().put("select_left_spot", new ActionNextSelect(-1));
        getActionMap().put("select_right_spot", new ActionNextSelect(+1));
    }

    /**
     * Selects a neighbour slot.
     * @param next The position of the neighbour slot relative to the current selected slot.
     *             In order to select the next right parking spot this value should be +1.
     */
    public void selectNeighbourSlot(int next) {
        if (selectedSpot != null) {
            int currentID = selectedSpot.getID() - 1;
            int targetID = currentID + next;

            if (targetID < parkingSpots.length && targetID >= 0) {
                selectedSpot.deselect();
                selectedSpot = parkingSpots[targetID];
                selectedSpot.select();
            }

        } else {
            //no shape active yet, just take the first
            selectedSpot = parkingSpots[0];
            selectedSpot.select();
        }
    }

    /**
     * Removes the given Tetromino from every other parking slot except the current selected one.
     * @param selectedTetromino The {@link TetrominoDraw} to be removed.
     */
    public void removeTetrominoFromOtherParkingSpots(TetrominoDraw selectedTetromino) {
        for (ParkingSpot spot : parkingSpots) {
            if (spot != selectedSpot && spot.getTetrominoDraw() == selectedTetromino) { //do not check the selected parking spot
                spot.freeParkingSpot();
            }
        }
    }

    /**
     * Triggers the resizing of the parking spots. Should be called after the kachel size of a tetromino is not changing anymore.
     */
    public void adaptSizeOfParkingSpots() {
        Dimension d = getSize();

        int parkingSpotsNr = userPreferences.getNumberOfParkingSpots();

        int totalWidthForParkingSpots = d.width - (parkingSpotsNr * MARGIN_BETWEEN_PARKING_SPOTS); //using the total width without the margin
        int widthPerParkingSpot = totalWidthForParkingSpots / parkingSpotsNr;

        int s = Math.min(d.height, widthPerParkingSpot);
        Dimension size = new Dimension(s, s);

        for (ParkingSpot parkingSpot : parkingSpots) {
            parkingSpot.setPreferredSize(size);
            parkingSpot.setMinimumSize(size);
            parkingSpot.setMaximumSize(size);
        }

        revalidate();
    }


    @Override
    public Insets getInsets() {
        //adding margins
        return new Insets(0, 5, 0, 5);
    }

    /**
     * Highlights the component as selected or removes the highlighting.
     *
     * @param hasFocus If true, component is highlighted, otherwise highlighting is removed.
     */
    void setFocus(boolean hasFocus) {
        if (hasFocus) {
            setBorder(BorderFactory.createMatteBorder(BORDER_STROKE, BORDER_STROKE, BORDER_STROKE, BORDER_STROKE, Color.RED));
        } else {
            setBorder(null);
            if (selectedSpot != null) {
                selectedSpot.deselect();
                selectedSpot = null;
            }
        }
    }

    /**
     * Removes the selection of all tetrominoes in every parking slot.
     */
    void removeTetrominoSelection() {
        for (ParkingSpot spot : parkingSpots) {
            spot.setTetrominosSelectionState(false);
        }
    }

}
