package tetris.puzzles.game;

import tetris.puzzles.actions.ActionMirror;
import tetris.puzzles.actions.ActionMove;
import tetris.puzzles.actions.ActionTurn;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.interfaces.TetrominoObserver;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static tetris.puzzles.tetromino.TetrominoDraw.TETROMINO_PATH_STORKE;

/**
 * This class is responsible for handling the parking spots where the tetrominoes can be parked.
 */
public class ParkingSpot extends JPanel {

    public static final int STORKE_OF_OUTER_BOARDER = 5;
    final int ID;
    boolean isSelected;
    TetrominoDraw addedTetromino;

    SizeObserver sizeObserver;
    TetrominoObserver tetrominoObserver;
    UserPreferences userPreferences;

    /**
     * Constructs a new parking spot.
     * @param tetrominoObserver A {@link TetrominoObserver} that can be used to get selected tetrominoes
     * @param sizeObserver  A {@link SizeObserver} that can be used to get the current size of tetrominoes
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     * @param id The ID of the parking spot used so that user can choose by a number on the keyboard.
     */
    ParkingSpot(TetrominoObserver tetrominoObserver, SizeObserver sizeObserver, UserPreferences userPreferences, int id) {
        this.tetrominoObserver = tetrominoObserver;
        this.sizeObserver = sizeObserver;
        this.userPreferences = userPreferences;
        this.ID = id;

        setBorder(BorderFactory.createMatteBorder(STORKE_OF_OUTER_BOARDER, STORKE_OF_OUTER_BOARDER, STORKE_OF_OUTER_BOARDER, STORKE_OF_OUTER_BOARDER, Color.BLACK));

        addSizeListener(sizeObserver, userPreferences);

        addClickListener();

        addKeyBindings();
    }

    /**
     * Adds a MouseListener that determines if the user clicked on a tetromino and handles the disselect / select logic.
     */
    private void addClickListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                Point clicked = e.getPoint();

                if (addedTetromino != null) {
                    if (addedTetromino.isPointInTetromino(clicked)) {
                        //user clicked tetromino

                        if (addedTetromino.isSelected()) {
                            //user wants to deselect tetromino
                            addedTetromino.setSelected(false);
                            setEnableStateKeyListener(false);
                            tetrominoObserver.setSelectedTetromino(null, false);
                        } else {
                            tetrominoObserver.deselectAllTetrominoesInParkingSpot(); //deselect other might previously selected tetrominoes in parking spots
                            addedTetromino.setSelected(true);
                            setEnableStateKeyListener(true);
                            tetrominoObserver.setSelectedTetromino(addedTetromino, false);
                        }
                        repaint();
                    }
                }
            }
        });
    }


    /**
     * Adds a size listener to the panel that is triggered when window resizes.
     * This method notifes the {@link SizeObserver} that the size of this component and thus it's inner size of tetromino's kacheln have changed.
     */
    private void addSizeListener(SizeObserver sizeObserver, UserPreferences userPreferences) {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                //size has been updated, so length of grid might also has changed.
                if (getWidth() != 0) {
                    sizeObserver.setGridLengthParkingSpot(getSquareGridLength(), userPreferences.getNumberOfKachelnInStone());
                }

            }
        });
    }

    /**
     * Returns the borders of the parking spot.
     * @return An int[4]-array with left, top, bottom, right coordinates of the borders.
     */
    public int[] getBorders() {
        Insets insets = getBorder().getBorderInsets(this);
        int[] borders = new int[4];
        borders[0] = insets.left - TETROMINO_PATH_STORKE / 2;
        borders[1] = insets.top - TETROMINO_PATH_STORKE / 2;
        borders[2] = getWidth() - insets.right + TETROMINO_PATH_STORKE / 2;
        borders[3] = getHeight() - insets.bottom + TETROMINO_PATH_STORKE / 2;
        return borders;
    }

    /**
     * Sets the selection state of the tetromino in the parking spot.
     * @param selectionState if ture, the tetromino in the parking spot is selected and otherwise deselected.
     */
    void setTetrominosSelectionState(boolean selectionState) {
        if (addedTetromino != null) {
            addedTetromino.setSelected(selectionState);
        }
        setEnableStateKeyListener(selectionState);
        repaint();
    }

    /**
     * Adds Key Bindings.
     */
    private void addKeyBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "move_tetromino_up");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "move_tetromino_up");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "move_tetromino_left");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "move_tetromino_left");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "move_tetromino_right");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "move_tetromino_right");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "move_tetromino_down");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move_tetromino_down");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("C"), "turn_tetromino_right");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Y"), "turn_tetromino_left");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "mirror_tetromino_horizontal");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("E"), "mirror_tetromino_vertical");


        getActionMap().put("move_tetromino_up", new ActionMove(0, -1));
        getActionMap().put("move_tetromino_left", new ActionMove(-1, 0));
        getActionMap().put("move_tetromino_right", new ActionMove(1, 0));
        getActionMap().put("move_tetromino_down", new ActionMove(0, 1));

        getActionMap().put("turn_tetromino_right", new ActionTurn(true));
        getActionMap().put("turn_tetromino_left", new ActionTurn(false));

        getActionMap().put("mirror_tetromino_horizontal", new ActionMirror(true));
        getActionMap().put("mirror_tetromino_vertical", new ActionMirror(false));

        setEnableStateKeyListener(false); //Key Bindings get only active when this component gets focused.
    }

    /**
     * Enables or disables all key bindings of this class.
     * @param stateKeyListener If true, all key bindings will be enabled. If false, all key bindings will be disabled.
     */
    public void setEnableStateKeyListener(boolean stateKeyListener) {
        Object[] keys = getActionMap().keys();
        for (Object key : keys) {
            Action action = getActionMap().get(key);
            action.setEnabled(stateKeyListener);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (addedTetromino != null) {
            //if tetromino is added
            addedTetromino.draw(g);
        }

        Color color = (isSelected) ? Color.RED : Color.BLACK;
        g.setColor(color);
        drawStringRightAligned(new Point(getWidth(), 0), "" + ID, color, g);

    }

    /**
     * Returns the squared grid length the component wants to use.
     * @return The grid length and width of the component.
     */
    private int getSquareGridLength() {
        Insets border = getBorder().getBorderInsets(this);
        return Math.min(getWidth() - border.left - border.right, getHeight() - border.top - border.bottom); //slot should be quadratic
    }

    /**
     * This is a helper method which takes a string and draws it on the right side of the canvas (right aligned).
     *
     * @param endPoint The coordinates where the string should end
     * @param message  The string to be displayed.
     * @param color
     * @param g        The {@link Graphics} object to use.
     */
    private void drawStringRightAligned(Point endPoint, String message, Color color, Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 20));

        // get the FontMetrics for the current font
        FontMetrics fm = g.getFontMetrics();

        // get the length and height of string
        int stringWidth = fm.stringWidth(message);
        int stringHeight = fm.getAscent();

        // calculate the position for the leftmost character in the baseline
        int x = endPoint.x - stringWidth - 5; //5px as margin
        int y = endPoint.y + stringHeight + 5;

        g.setColor(color);
        g.drawString(message, x, y);
    }

    /**
     * Selects this parking spot.
     */
    void select() {
        isSelected = true;
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.RED));
        repaint();
    }

    /**
     * Deselects this parking spot.
     */
    void deselect() {
        isSelected = false;
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        repaint();
    }

    /**
     * Removes tetromino from the parking spot.
     */
    public void freeParkingSpot() {
        addedTetromino = null;
        setEnableStateKeyListener(false);
        repaint();
    }

    /**
     * Inserts new Tetromino into the parking spot if it is free, otherwise tetromino is not inserted and false is returned by this method.
     * @param tetrominoDraw The {@link TetrominoDraw} to be inserted into the parking spot.
     * @return True, if tetromino could be inserted, false otherwise.
     */
    public boolean insertNewTetromino(TetrominoDraw tetrominoDraw) {
        if (addedTetromino == null) {
            addedTetromino = tetrominoDraw;

            centerTetrominoInParkingSpot(tetrominoDraw);

            setTetrominosSelectionState(true);
            return true;
        } else {
            //parking slot is already taken
            return false;
        }
    }

    /**
     * Centers a new inserted tetromino in the parking spot if there is enough space available.
     * @param tetrominoDraw The {@link TetrominoDraw} to be centered in the parking spot.
     */
    private void centerTetrominoInParkingSpot(TetrominoDraw tetrominoDraw) {
        Insets boarderInsets = getBorder().getBorderInsets(this);
        int[] tetrominoBounds = tetrominoDraw.getBounds();
        int tetrominoWidth = tetrominoBounds[2] - tetrominoBounds[0] + TETROMINO_PATH_STORKE;
        int tetrominoHeight = tetrominoBounds[3] - tetrominoBounds[1] + TETROMINO_PATH_STORKE;

        int parkingSpotWidth = getWidth() - boarderInsets.left - boarderInsets.right;
        int parkingSportHeight = getHeight() - boarderInsets.top - boarderInsets.bottom;

        int notUsedWidth = parkingSpotWidth - tetrominoWidth;
        int notUsedHeight = parkingSportHeight - tetrominoHeight;

        int centerX = notUsedWidth / 2;
        int centerY = notUsedHeight / 2;
        Point startCoordinates;
        if (centerX > sizeObserver.getTetrominoKachelSize() && centerY > sizeObserver.getTetrominoKachelSize()) {
            startCoordinates = new Point(centerX, centerY);
        } else {
            // otherwise we even have not a kachel space to the left / right side when we would insert the tetromino into the center
            // so instead insert into the top left corner
            startCoordinates = new Point(boarderInsets.left, boarderInsets.top);
        }

        addedTetromino.setStartCoordinates(startCoordinates);
    }

    public TetrominoDraw getTetrominoDraw() {
        return addedTetromino;
    }

    public int getID() {
        return ID;
    }

}
