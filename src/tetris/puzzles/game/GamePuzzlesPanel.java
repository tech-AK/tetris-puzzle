package tetris.puzzles.game;

import tetris.puzzles.datamodels.SizeSaver;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.ControlInterface;
import tetris.puzzles.interfaces.GameInterface;
import tetris.puzzles.interfaces.PauseObserver;
import tetris.puzzles.interfaces.TetrominoObserver;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;


/**
 * This class is responsible for handling the left (overall) game screen where the user can play.
 */
public class GamePuzzlesPanel extends JPanel implements GameInterface, TetrominoObserver {

    GameGrid gameGrid;
    ParkingSpotHolder parkingSpotHolder;
    ShapeHolder shapeHolder;
    ControlInterface controlInterface;

    ArrayList<TetrominoDraw> tetrominoesInGrid;

    boolean isShapeHolderSelected;
    boolean isGamePaused;
    boolean isGameOverDisabled;

    TetrominoDraw selectedTetromino;

    PauseObserver pauseObserver;

    /**
     * Constructs the GamePuzzlesPanel.
     * @param pauseObserver A {@link PauseObserver} that can be called to pause the game.
     */
    public GamePuzzlesPanel(PauseObserver pauseObserver) {
        this.pauseObserver = pauseObserver;
        setBackground(Color.WHITE);

        tetrominoesInGrid = new ArrayList<>();

        addKeyBindings();

        addResizeListener();
    }

    public UserPreferences getUserPreferences() {
        return gameGrid == null ? null : gameGrid.getUserPreferences();
    }

    /**
     * Adds a size listener to the panel that is triggered when window resizes.
     * Note: Everytime the windows resizes and the tetrominoes have to be replaced in the grid, the check for a game over is temporarily disabled.
     */
    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                //trigger resizing of components if game is already started
                if (gameGrid != null) {
                    onTetrominoSizeHasChanged();
                }
            }
        });
    }

    /**
     * Adds KeyBindings to the panel.
     */
    private void addKeyBindings() {
        Action switchSelectedComponent = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameGrid != null) {
                    //if game already started
                    if (isShapeHolderSelected) {
                        selectParkingSlot();
                    } else {
                        //if ParkingSlot is selected or no selection, then select shapeHolder
                        selectShapeHolder();
                    }
                }

            }
        };

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("V"), "V_pressed");
        getActionMap().put("V_pressed", switchSelectedComponent);
    }

    /**
     * Selects the shape holder component.
     */
    private void selectShapeHolder() {
        shapeHolder.setFocus(true);
        shapeHolder.setFocusable(true);
        parkingSpotHolder.setFocus(false);
        parkingSpotHolder.setFocusable(false);
        shapeHolder.requestFocus();
        isShapeHolderSelected = true;
    }

    /**
     * Selects the parking slot component.
     */
    private void selectParkingSlot() {
        shapeHolder.setFocus(false);
        shapeHolder.setFocusable(false);
        parkingSpotHolder.setFocus(true);
        parkingSpotHolder.setFocusable(true);
        parkingSpotHolder.requestFocus();
        isShapeHolderSelected = false;
    }


    /**
     * Creates a new Game Layout.
     * @param userPreferences The {@link UserPreferences} to be used in the game
     */
    private void createGameLayout(UserPreferences userPreferences) {
        if (gameGrid != null) { //a game is already running
            onGameOver(); //trigger saving of highscore of running game
        }

        removeAll();

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints gameGrid_c = new GridBagConstraints();
        GridBagConstraints shapeHolder_c = new GridBagConstraints();
        GridBagConstraints parkingSpotHolder_c = new GridBagConstraints();

        SizeSaver tetrominoSizeSaver = new SizeSaver();
        tetrominoSizeSaver.addObserver(this);

        gameGrid = new GameGrid(this, tetrominoSizeSaver, userPreferences);
        parkingSpotHolder = new ParkingSpotHolder(this, controlInterface, tetrominoSizeSaver, userPreferences);
        shapeHolder = new ShapeHolder(this, controlInterface, tetrominoSizeSaver, userPreferences);

        gameGrid_c.weightx = 1;
        gameGrid_c.weighty = 1;
        gameGrid_c.gridy = 0;
        gameGrid_c.gridx = 0;
        gameGrid_c.fill = GridBagConstraints.BOTH; //stretch game grid also vertically so that it takes up all available space
        gameGrid_c.insets = new Insets(0, 0, 0, 0);

        shapeHolder_c.weightx = 1;
        shapeHolder_c.weighty = 0;
        shapeHolder_c.gridy = 1;
        shapeHolder_c.gridx = 0;
        shapeHolder_c.fill = GridBagConstraints.HORIZONTAL;
        shapeHolder_c.insets = new Insets(10, 0, 0, 0);

        parkingSpotHolder_c.weightx = 1;
        parkingSpotHolder_c.weighty = 0;
        parkingSpotHolder_c.gridy = 2;
        parkingSpotHolder_c.gridx = 0;
        parkingSpotHolder_c.fill = GridBagConstraints.HORIZONTAL;
        parkingSpotHolder_c.insets = new Insets(5, 0, 5, 0);

        add(gameGrid, gameGrid_c);
        add(shapeHolder, shapeHolder_c);
        add(parkingSpotHolder, parkingSpotHolder_c);

        revalidate();

    }

    /**
     * Pauses the game.
     */
    public void pauseGame() {
        if (gameGrid != null) {
            gameGrid.onGamePaused();
            isGamePaused = true;
        }

    }

    /**
     * Continues the game.
     */
    public void continueGame() {
        if (isGamePaused) {
            gameGrid.onGameContinued();
            isGamePaused = false;
        }
    }

    @Override
    public void onGameStart(UserPreferences userPreferences) {
        //reset previous states as new game started
        isShapeHolderSelected = false;
        isGamePaused = false;
        tetrominoesInGrid.clear();
        isGameOverDisabled = false;
        selectedTetromino = null;

        if (gameGrid != null) {
            //User pushed start button, but game was already initialised.
            gameGrid.onGameOver(); //End the previous game
            pauseObserver.continueGame(); //Reset pause screen
        }

        createGameLayout(userPreferences);
        requestFocus();
    }

    @Override
    public void setControlInterface(ControlInterface controlInterface) {
        this.controlInterface = controlInterface;
    }

    @Override
    public void setSelectedTetromino(TetrominoDraw tetrominoDraw, boolean setByGameGrid) {
        selectedTetromino = tetrominoDraw;

        //Notify other components that new selection was made by user
        if (setByGameGrid) {
            parkingSpotHolder.removeTetrominoSelection();
        } else {
            //new tetromino was set by parking spot, so notify gameGrid to remove it's selection
            gameGrid.removeTetrominoSelection();
        }
    }

    @Override
    public TetrominoDraw getSelectedTetromino() {
        return selectedTetromino;
    }

    @Override
    public ArrayList<TetrominoDraw> getTetrominoesInGameGrid() {
        return new ArrayList<>(tetrominoesInGrid);
    }

    @Override
    public TetrominoDraw getTetrominoesInGameGridAtIndex(int index) {
        if (index >= tetrominoesInGrid.size()) {
            return null;
        } else {
            return tetrominoesInGrid.get(index);
        }
    }

    @Override
    public void addTetrominoToGameGridList(TetrominoDraw tetrominoDraw) {
        tetrominoesInGrid.add(tetrominoDraw);
    }

    @Override
    public void removeTetromino(TetrominoDraw tetrominoDraw) {
        tetrominoesInGrid.remove(tetrominoDraw);
        parkingSpotHolder.removeTetrominoFromOtherParkingSpots(tetrominoDraw);
    }

    @Override
    public void deselectAllTetrominoesInParkingSpot() {
        parkingSpotHolder.removeTetrominoSelection();
    }

    @Override
    public void onGameOver() {
        if (!isGameOverDisabled) {
            if (gameGrid == null) {
                //user exit game even before starting it. No need to do anything.
                return;
            }
            gameGrid.onGameOver();

            remove(gameGrid);
            remove(parkingSpotHolder);
            remove(shapeHolder);

            tetrominoesInGrid.clear();

            JLabel label = new JLabel("GAME OVER");
            label.setFont(new Font("Arial", Font.BOLD, 45));
            label.setForeground(Color.RED);
            add(label);

            revalidate();
            repaint();

            controlInterface.onGameOver(gameGrid.userPreferences); //here a popup is created which blocks the thread, so need to call this line last

        }
    }

    @Override
    public void disableGameOver() {
        isGameOverDisabled = true;
    }

    @Override
    public void enableGameOver() {
        isGameOverDisabled = false;
    }

    @Override
    public void onTetrominoSizeHasChanged() {
        /* Getting the size of the whole game component and setting the size of the GameGrid (ca. 60% of the screen height),
         * ParkingSlotHolder (20%) and ShapeHolder (20%) here.
         *
         * We need to use this approach instead of the automatically layout weights of GridBagLayout, as
         * GridBagLayout's weight only work, if there is left space that should be distributed.
         * However, our ParkingSlotHolder and ShapeHolder do not have a fixed space they need and thus, GridBagLayout
         * cannot work. Instead, we have to manually set the size of for the ParkingSlotHolder and ShapeHolder and
         * THEN can apply the weights to determine that GameGrid should take the rest of the available space.
         */
        Dimension size = getSize();
        parkingSpotHolder.setSize(size.width, (int) (size.height * 0.2));
        shapeHolder.setSize(size.width, (int) (size.height * 0.2));
        //now the getHeight() and getWidth() inside both components return valid values they can use to lay themselves out.

        //Notify components to change their size as they have now valid getHeight() and getWidth() values.
        parkingSpotHolder.adaptSizeOfParkingSpots();
        shapeHolder.adaptSizeOfShapes();

    }

}
