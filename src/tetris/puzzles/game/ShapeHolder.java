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

/**
 * This class is responsible for holding each shape.
 */
public class ShapeHolder extends JPanel {

    static final int MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL = 10;
    public static final int BORDER_STROKE = 5;
    UserPreferences userPreferences;
    Shape[] shapes;
    Shape selectedShape;

    TetrominoDraw lastFittedTetromino;
    Shape lastFittedShape;

    SizeObserver sizeObserver;
    TetrominoObserver tetrominoObserver;
    ControlInterface controlInterface;

    /**
     * Constructs a new ShapeHolder.
     * @param tetrominoObserver A {@link TetrominoObserver} that can be used to get selected tetrominoes
     * @param controlInterface A {@link ControlInterface} that can be used to display a warning to the user on forbidden operations.
     * @param sizeObserver A {@link SizeObserver} that can be used to get the current size of tetrominoes
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     */
    ShapeHolder(TetrominoObserver tetrominoObserver, ControlInterface controlInterface, SizeObserver sizeObserver, UserPreferences userPreferences) {
        this.tetrominoObserver = tetrominoObserver;
        this.sizeObserver = sizeObserver;
        this.controlInterface = controlInterface;
        this.userPreferences = userPreferences;

        setBackground(Color.WHITE);

        addShapes();

        addKeyBindings();

        addKeyListeners();
    }

    /**
     * Adds Key Listeners.
     */
    private void addKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);

                int pressedNumber = Character.getNumericValue(e.getKeyChar());
                if (pressedNumber > 0 && pressedNumber <= userPreferences.getNumberOfShapes()) {
                    selectShapeByNumber(pressedNumber);
                } else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    tryToFillShape();
                }

            }
        });
    }

    /**
     * Selects the shape with the corresponding number.
     * @param pressedNumber The number that was pressed
     */
    private void selectShapeByNumber(int pressedNumber) {
        if (selectedShape != null) {
            //deselect previous selected shape
            selectedShape.deselect();
        }

        selectedShape = shapes[pressedNumber - 1];
        selectedShape.select();
    }

    /**
     * Tries to fill the shape by the currently selected tetromino.
     */
    private void tryToFillShape() {
        if (selectedShape != null) {
            TetrominoDraw selectedTetromino = tetrominoObserver.getSelectedTetromino();
            if (selectedTetromino != null) {
                boolean tetrominoFits = selectedShape.insertNewTetromino(selectedTetromino);
                if (tetrominoFits) {
                    tetrominoObserver.removeTetromino(selectedTetromino);

                    //tetromino cannot be selected anymore, as it is fixed in shape
                    tetrominoObserver.setSelectedTetromino(null, false);
                    lastFittedTetromino = selectedTetromino;
                    lastFittedShape = selectedShape;
                } else {
                    controlInterface.displayWarning();
                }
            } else {
                if (selectedShape == lastFittedShape) {
                    //user has no new tetromino selected, but the same shape. S/he tries to re-position the last tetromino!
                    selectedShape.chooseOtherPositionForTetromino(lastFittedTetromino);
                }
            }
        }
    }

    /**
     * Adds shapes to the shape holder.
     */
    private void addShapes() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        shapes = new Shape[userPreferences.getNumberOfShapes()];


        GridBagConstraints gbc_shape_in_between = new GridBagConstraints();
        gbc_shape_in_between.insets = new Insets(BORDER_STROKE, 0, BORDER_STROKE, MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL);

        GridBagConstraints gbc_first_shape = new GridBagConstraints();
        gbc_first_shape.insets = new Insets(BORDER_STROKE, MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL / 2, BORDER_STROKE, MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL);

        GridBagConstraints gbc_last_shape = new GridBagConstraints();
        gbc_last_shape.insets = new Insets(BORDER_STROKE, 0, BORDER_STROKE, MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL / 2);


        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = new Shape(controlInterface, sizeObserver, i + 1, userPreferences);
            if (i == 0) {
                add(shapes[i], gbc_first_shape);
            } else if (i == shapes.length - 1) {
                add(shapes[i], gbc_last_shape);
            } else {
                add(shapes[i], gbc_shape_in_between);
            }
        }
    }

    /**
     * Selects a neighbour shape.
     * @param next The position of the neighbour shape relative to the current selected slot.
     *             In order to select the next right shape this value should be +1.
     */
    public void selectNeighbourShape(int next) {
        if (selectedShape != null) {
            int currentID = selectedShape.getID() - 1;
            int targetID = currentID + next;

            if (targetID < shapes.length && targetID >= 0) {
                selectedShape.deselect();
                selectedShape = shapes[targetID];
                selectedShape.select();
            }

        } else {
            //no shape active yet, just take the first
            selectedShape = shapes[0];
            selectedShape.select();
        }
    }

    /**
     * Adds Key Bindings (additionally to the key listener as otherwise the key bindings of the game grid would consume our keys even if this component is focused).
     */
    private void addKeyBindings() {
        //override the GameGrid consumption of left/right arrows if this component is focused
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "select_left_shape");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "select_right_shape");

        getActionMap().put("select_left_shape", new ActionNextSelect(-1));
        getActionMap().put("select_right_shape", new ActionNextSelect(+1));
    }

    /**
     * Triggers the resizing of the shapes. Should be called after the kachel size of a tetromino is not changing anymore.
     */
    public void adaptSizeOfShapes() {
        Dimension d = getSize();

        int shapesNr = userPreferences.getNumberOfShapes();

        int totalWidthForShapes = d.width - (shapesNr * MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL); //using the total width without the margin
        int widthPerShape = totalWidthForShapes / shapesNr;
        int height = getHeight();

        for (Shape shape : shapes) {
            Dimension size = new Dimension(widthPerShape, height);
            shape.setPreferredSize(size);
            shape.setMinimumSize(size);
            shape.setMaximumSize(size);
        }

        revalidate();
        repaint();
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
            if (selectedShape != null) {
                selectedShape.deselect();
                selectedShape = null;
            }
        }
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, MARGIN_BETWEEN_TWO_SHAPES_IN_PIXEL / 2, 0, 0);
    }

}
