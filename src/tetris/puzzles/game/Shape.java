package tetris.puzzles.game;

import tetris.tools.Tetromino;
import tetris.tools.TetrominoArraylist;
import tetris.puzzles.datamodels.MatrixCoordinate;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.ControlInterface;
import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.tetromino.ShapeDraw;
import tetris.puzzles.tetromino.TetrominoArray;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Random;

import static tetris.puzzles.tetromino.TetrominoDraw.TETROMINO_PATH_STORKE;

/**
 * This class is responsible for creating the shapes which have the capability to hold two different tetrominoes.
 * <br><b>NOTE:</b> The internal array for representing the grid differs from the array proposed in the documentation as different values are used:
 * <ul>
 *  <li> Value 0 --> the cell is not used by the shape nor the tetrominoes.</li>
 *  <li> Value 1 --> the cell is used by the shape and is reserved for one tetromino to be fitted into this cell.</li>
 *  <li> Value 2 --> this cell was used by the shape and is currently assigned to the first tetromino added to the shape</li>
 *  <li> Value 3 --> this cell was used by the shape and is currently assigned to the second tetromino added to the shape</li>
 *  </ul>
 *  <br>In order to determine whether a tetromino T fits into a shape S, the following algorithm is used:
 *  <ol>
 *  <li>Extract the relative coordinates of T by using {@link #calculateRelativeArray(TetrominoArray)}</li>
 *  <li>For the shape's array go through every cell and check whether the relative coordinates can be applied (i. e. if the shape's array has enough 1's at the places the tetromino need).
 *  If {@code true}, save the found possibility. The second step is done by {@link #getPossibilitiesForAdding(TetrominoArray, int[][], int, boolean)} method.</li>
 *  <li>For every found possibility, check whether using it would still leave a shape array that can be used to fit another tetromino into it by using {@link #isShapeStillConnected(MatrixCoordinate, ArrayList)}.</li>
 *  </ol>
 */
public class Shape extends JPanel {
    public static final int MARGIN_AROUND_ID_DESCRIPTION = 2;
    public static final int MARGIN_FROM_ID_DESCRIPTION_TO_SHAPE = 5;

    UserPreferences userPreferences;
    final int ID;
    boolean isSelected;

    int[][] internalShapeArray;
    int[][] originalShapeArray;

    ShapeDraw shapeDraw;

    int indexOfLastFittedTetromino = 0;
    TetrominoDraw lastFittedTetromino;

    ArrayList<MatrixCoordinate> possibilitiesForLastAddedTetromino;
    int lastUsedIndex = -1;
    int stringHeight;

    SizeObserver sizeObserver;
    ControlInterface controlInterface;

    /**
     * Constructs a new Shape object.
     * @param controlInterface A {@link ControlInterface} that can be used to display a warning to the user on forbidden operations.
     * @param sizeObserver A {@link SizeObserver} that can be used to get the current size of tetrominoes
     * @param id  The ID of the shape used so that user can choose by a number on the keyboard.
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     */
    Shape(ControlInterface controlInterface, SizeObserver sizeObserver, int id, UserPreferences userPreferences) {
        this.controlInterface = controlInterface;
        this.sizeObserver = sizeObserver;
        this.userPreferences = userPreferences;
        this.ID = id;

        setBorder(BorderFactory.createRaisedBevelBorder());

        internalShapeArray = getRandomShapeArray(userPreferences);
        originalShapeArray = deepCopyArray(internalShapeArray); //create a copy in order to enable resetting more faster

        shapeDraw = new ShapeDraw(internalShapeArray, sizeObserver);

        addResizeListener();
    }

    /**
     * Adds a listener to the resizing of the panel.
     * Notifies the {@link SizeObserver} that panel size and thus, kachel size of tetromino has changed.
     */
    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                //size has been updated, so length of grid might also has changed.
                int gridLength = getGridLength(getWidth(), getHeight());
                if (getWidth() != 0) {
                    sizeObserver.setGridLengthShape(gridLength, userPreferences.getNumberOfKachelnInStone());
                }

                repaint();

            }
        });
    }

    public int getID() {
        return ID;
    }

    /**
     * Returns a random Shape array that can be used for {@link ShapeDraw}.
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     * @return The int[][] array containing the random Shape array.
     */
    private int[][] getRandomShapeArray(UserPreferences userPreferences) {
        TetrominoArray firstTetromino = getRandomTetrominoArray(userPreferences);
        TetrominoArray secondTetromino = getRandomTetrominoArray(userPreferences);

        int[][] resultArray;

        //search for two compatible tetrominoes
        while (true) {
            resultArray = combineTwoTetrominosIntoAShape(firstTetromino, secondTetromino);
            if (resultArray != null) {
                //found one valid solution
                break;
            }
            //no valid solution, need to use other tetromino
            secondTetromino = getRandomTetrominoArray(userPreferences);
        }

        return resultArray;
    }

    /**
     * Returns a random {@link TetrominoArray}.
     * @param userPreferences The {@link UserPreferences} in order to determine the grid size.
     * @return A random {@link TetrominoArray}
     */
    private TetrominoArray getRandomTetrominoArray(UserPreferences userPreferences) {
        TetrominoArraylist spielsteinArraylist = new TetrominoArraylist();
        ArrayList<Tetromino> tetrominoList = spielsteinArraylist.alleEinbettungenStandardreihenfolgeRek(userPreferences.getNumberOfKachelnInStone());

        Random random = new Random();
        int randomIndex = random.nextInt(tetrominoList.size());

        return new TetrominoArray(tetrominoList.get(randomIndex));
    }

    /**
     * Handles the insertion of a second tetromino into a other tetromino in order to create a new shape array.
     * @param firstTetromino The {@link TetrominoArray} of the first tetromino.
     * @param secondTetromino The {@link TetrominoArray} of the second tetromino.
     * @return int[][] array of a possible shape if combination of both tetrominoes was possible or null otherwise.
     */
    int[][] combineTwoTetrominosIntoAShape(TetrominoArray firstTetromino, TetrominoArray secondTetromino) {

        //check if second tetromino can be fitted into k x k grid
        ArrayList<MatrixCoordinate> possibilitiesToAddTetromino = getPossibilitiesForAdding(secondTetromino, firstTetromino.getInternalArray(), 0, false, false);

        if (possibilitiesToAddTetromino.size() > 0) {
            //found one possibility to add tetromino into grid
            MatrixCoordinate tetrominosStartCoordinateInGrid = possibilitiesToAddTetromino.get(0);

            int[][] testArray = deepCopyArray(firstTetromino.getInternalArray());
            //fill second tetromino into the grid of the first tetromino
            fitTetrominoIntoArray(secondTetromino, tetrominosStartCoordinateInGrid, testArray, 1);

            //check for holes
            TetrominoArray testShape = new TetrominoArray(testArray);
            if (testShape.hasHole() || testShape.hasBigHole()) {
                return null;
            } else {
                //no holes, we can use our testArray
                return testArray;
            }

        }

        return null;
    }


    /**
     * This method should be called when a new Tetromino is inserted into the shape.
     *
     * @param tetromino A {@link TetrominoDraw} object to insert.
     * @return True, if tetromino could be inserted. False otherwise.
     */
    boolean insertNewTetromino(TetrominoDraw tetromino) {
        if (indexOfLastFittedTetromino == 0 || tetromino == lastFittedTetromino) {
            //there is no tetromino added yet, or the user has pressed enter again, so that we got the same tetromino again
            return insertFirstTetromino(tetromino);
        } else {
            return insertSecondTetromino(tetromino);
        }
    }

    /**
     * Triggers to choose another valid position for the tetromino.
     * @param tetromino The {@link TetrominoDraw} that should get another position.
     */
    void chooseOtherPositionForTetromino(TetrominoDraw tetromino) {
        if (lastFittedTetromino != null && lastFittedTetromino == tetromino && possibilitiesForLastAddedTetromino.size() > 1) {
            //that means that the method is called again for the same selected tetromino, so displaying another possibility.
            resetMatrix();
            int randomIndex = getRandomIndex(possibilitiesForLastAddedTetromino.size(), lastUsedIndex);
            MatrixCoordinate tetrominosStartCoordinateInGrid = possibilitiesForLastAddedTetromino.get(randomIndex);

            fitTetrominoIntoArray(tetromino, tetrominosStartCoordinateInGrid, internalShapeArray, indexOfLastFittedTetromino + 1);
            lastUsedIndex = randomIndex;

            repaint();
        }
    }

    /**
     * Inserts the first tetromino into the shape.
     * @param tetromino A {@link TetrominoDraw} object to insert.
     * @return True, if tetromino could be inserted. False otherwise.
     */
    private boolean insertFirstTetromino(TetrominoDraw tetromino) {
        ArrayList<MatrixCoordinate> possibilitiesToAddTetromino = getPossibilitiesForAdding(tetromino, internalShapeArray, 1, true, true);

        if (possibilitiesToAddTetromino.size() > 0) {
            int randomIndex = getRandomIndex(possibilitiesToAddTetromino.size(), lastUsedIndex);
            MatrixCoordinate tetrominosStartCoordinateInGrid = possibilitiesToAddTetromino.get(randomIndex);

            indexOfLastFittedTetromino++;
            shapeDraw.setColor(indexOfLastFittedTetromino, tetromino.getTetrominoColor());

            fitTetrominoIntoArray(tetromino, tetrominosStartCoordinateInGrid, internalShapeArray, indexOfLastFittedTetromino + 1);
            repaint();

            lastFittedTetromino = tetromino;
            possibilitiesForLastAddedTetromino = possibilitiesToAddTetromino;
            lastUsedIndex = randomIndex;
            return true;
        }
        return false;
    }


    /**
     * Inserts the second tetromino into the shape.
     * @return True, if tetromino could be inserted. False otherwise.
     */
    private boolean insertSecondTetromino(TetrominoDraw tetromino) {
        ArrayList<MatrixCoordinate> possibilitiesToAddTetromino = getPossibilitiesForAdding(tetromino, internalShapeArray, 1, false, false);

        if (possibilitiesToAddTetromino.size() > 0) {
            MatrixCoordinate tetrominosStartCoordinateInGrid = possibilitiesToAddTetromino.get(0);
            indexOfLastFittedTetromino++;
            shapeDraw.setColor(indexOfLastFittedTetromino, tetromino.getTetrominoColor());

            fitTetrominoIntoArray(tetromino, tetrominosStartCoordinateInGrid, internalShapeArray, indexOfLastFittedTetromino + 1);
            repaint();

            secondTetrominoWasFitted();
            return true;
        }
        return false;
    }

    /**
     * Call this method if two tetrominoes were fitted correctly. Handles adding points to score and animation.
     */
    private void secondTetrominoWasFitted() {
        controlInterface.addPoints(userPreferences);

        shapeDraw.startFadeOutAnimation(this);
    }

    /**
     * Called when FadeOut Animation of the shape has finished.
     */
    public void onFadeOutAnimationFinished() {
        shapeDraw.resetAlpha();

        //Reset everything and prepare for next tetrominoes.
        resetMatrix();
        lastUsedIndex = -1;
        lastFittedTetromino = null;
        possibilitiesForLastAddedTetromino = null;
        indexOfLastFittedTetromino = 0;
        repaint();
    }

    /**
     * Returns a random index in the specified range excluding the specified excludeIndex.
     * @param range The specified range. In order to return a value the interval [0, range] is used.
     * @param excludeIndex An index that should not be returned.
     * @return An index in the range [0, range] but definitely not excludeIndex. Returns -1 if not possible.
     */
    private int getRandomIndex(int range, int excludeIndex) {
        if (range < 0 || (range == 0 && excludeIndex == 0)) {
            return -1;
        }

        Random random = new Random();
        int randomIndex = -1;

        do {
            randomIndex = random.nextInt(range);
        } while (randomIndex == excludeIndex);


        return randomIndex;
    }

    /**
     * Resets the internal matrix of a shape back to the original.
     */
    private void resetMatrix() {
        internalShapeArray = deepCopyArray(originalShapeArray);
        shapeDraw.updateInternalArray(internalShapeArray);
    }

    /**
     * Fits a tetromino into the given array at that given start cell in the array with the given fill value.
     * @param tetromino The tetromino to be fitted into the array.
     * @param tetrominoStartCell The start cell where the the first kachel of the tetromino should be added.
     * @param internalShapeArray The array in which the tetromino should be fitted into.
     * @param fillValue The fill value that should used to fit the tetromino.
     */
    private void fitTetrominoIntoArray(TetrominoArray tetromino, MatrixCoordinate tetrominoStartCell, int[][] internalShapeArray, int fillValue) {
        ArrayList<MatrixCoordinate> relativeCoordinatesOfTetromino = calculateRelativeArray(tetromino);

        internalShapeArray[tetrominoStartCell.row][tetrominoStartCell.column] = fillValue;
        for (MatrixCoordinate nextCoordinate : relativeCoordinatesOfTetromino) {
            internalShapeArray[tetrominoStartCell.row + nextCoordinate.row][tetrominoStartCell.column + nextCoordinate.column] = fillValue;
        }
    }

    /**
     * Returns every possibility to add an tetromino into the given array so that it fills out only grids with the specified kachelValue.
     * The possibilities are returned as an ArrayList which hold the absolute {@link MatrixCoordinate} for the first kachel of the given tetromino.
     *
     * @param tetrominoArray     The array of the tetromino that should be fitted into the arrayToChange
     * @param arrayToChange      The array that contains the matrix that should be check for a possible insertion of the tetromino.
     * @param kachelValue        The kachelValue that the new inserted Tetromino is allowed to overwrite.
     *                           E. g. if adding a new tetromino into a shape, the kachelValue should be 1, as the new tetromino should only fill cells in the array that
     *                           are placeholders (i. e. contain the value 1). If searching for a possible shape, the kachelValue should be 0 as the tetromino should be only
     *                           added where the other tetromino is not added yet.
     * @param getAllSolutions    If false, this method returns directly if a first solution is found. Thus, if true, the ArrayList has a size between 0 and 1.
     *                           E. g. if this is the second tetromino added, there is only one valid solution, so can directly break this procedure after finding the first solution.
     * @param checkForConnection If true, for every found solution, there is a check included that tests whether the after inserting the tetromino every kachel can still be traveled by a connected path.
     *                           E. g. this should be true, if adding first tetromino but false if adding a shape (as for shapes there is automatically a hasHole() check) or the second tetromino.
     * @return An ArrayList that contains valid solutions.
     */
    private ArrayList<MatrixCoordinate> getPossibilitiesForAdding(TetrominoArray tetrominoArray, int[][] arrayToChange, int kachelValue, boolean getAllSolutions, boolean checkForConnection) {
        ArrayList<MatrixCoordinate> possibilitiesToAddTetromino = new ArrayList<>();

        ArrayList<MatrixCoordinate> relativeCoordinatesForInsertingTetromino = calculateRelativeArray(tetrominoArray);

        for (int shapeRow = 0; shapeRow < arrayToChange.length; shapeRow++) {
            for (int shapeCol = 0; shapeCol < arrayToChange[shapeRow].length; shapeCol++) {

                if (arrayToChange[shapeRow][shapeCol] == kachelValue) {
                    //found cell that contains the kachelValue we are allowed to overwrite
                    boolean tetrominoCanBeFitted = true;

                    //check if from this first cell all relative coordinates ("connected path") of the tetromino could be applied.
                    for (MatrixCoordinate relativeCoordinates : relativeCoordinatesForInsertingTetromino) {
                        int row = shapeRow + relativeCoordinates.row;
                        int col = shapeCol + relativeCoordinates.column;

                        if (row < 0
                                || row >= arrayToChange.length
                                || col < 0
                                || col >= arrayToChange[shapeRow].length
                                || arrayToChange[row][col] != kachelValue) {
                            tetrominoCanBeFitted = false;
                            break;
                        }
                    }

                    if (tetrominoCanBeFitted) {
                        MatrixCoordinate testedCoordinate = new MatrixCoordinate(shapeRow, shapeCol);
                        if (checkForConnection) {
                            if (isShapeStillConnected(testedCoordinate, relativeCoordinatesForInsertingTetromino)) {
                                // Found one possibility!
                                possibilitiesToAddTetromino.add(testedCoordinate);
                            }
                        } else {
                            //do not check for connection and directly add to list.
                            possibilitiesToAddTetromino.add(testedCoordinate);
                        }

                        if (!getAllSolutions) {
                            return possibilitiesToAddTetromino; //just directly return the first possibility found, as only one possibility can be left.
                        }
                    }
                }
            }
        }
        return possibilitiesToAddTetromino;
    }

    /**
     * Checks if the shape is still connected after inserting a tetromino into it.
     * @param startCoordinate The coordinates of the start cell where the tetromino was fitted into.
     * @param relativeCoordinatesList The relative coordinates of all cells starting from the startCoordiante.
     * @return True, if shape is still connected, i. e. a connect path exists.
     */
    private boolean isShapeStillConnected(MatrixCoordinate startCoordinate, ArrayList<MatrixCoordinate> relativeCoordinatesList) {

        //Create a new array, simulate the insertion of tetromino and check if the reminding places to be filled are still connected.
        //FIRST: Remove the cells that the current tetromino would fill (i. e. simulate that tetromino was inserted).
        int[][] arrayCopy = deepCopyArray(internalShapeArray);
        arrayCopy[startCoordinate.row][startCoordinate.column] = 0;
        for (MatrixCoordinate relativeCoordinates : relativeCoordinatesList) {
            arrayCopy[startCoordinate.row + relativeCoordinates.row][startCoordinate.column + relativeCoordinates.column] = 0;
        }

        //SECOND: Transform the arrayCopy into an array which holds the minimal embedding
        TetrominoArray tetrominoArray = new TetrominoArray(arrayCopy);
        tetrominoArray.moveToMinimalEmbedding();

        //THIRD: Check if every cell with a 1 is part of an connected path.
        return isEnoughPlaceForOtherTetromino(tetrominoArray);
    }

    /**
     * As 2D-array are containing references to other arrays, usual cloning would only copy the references to the array but NOT
     * the actual values. Use this method to perform a deep cloning of the array.
     *
     * @param arrayToCopy The array that should be cloned
     * @return The cloned array. It is absolute identical to the template.
     */
    int[][] deepCopyArray(int[][] arrayToCopy) {
        int[][] clone = new int[arrayToCopy.length][];
        for (int i = 0; i < arrayToCopy.length; i++) {
            clone[i] = arrayToCopy[i].clone();
        }
        return clone;
    }

    /**
     * Returns an ArrayList holding {@link MatrixCoordinate} which contain the relative row and column index of all
     * kacheln that are connected to the first kachel found in the first row.
     * <i>This can be used as a shortcut method in order to avoid needing to process the whole array of the spielstein in order
     * to check if the tetromino would fit into shape.</i>
     *
     * @param tetrominoArray The tetromino that should be processed.
     * @return An ArrayList holding {@link MatrixCoordinate} relative to the first kachel found in the first row.
     */
    private ArrayList<MatrixCoordinate> calculateRelativeArray(TetrominoArray tetrominoArray) {
        tetrominoArray.moveToMinimalEmbedding();

        //As tetromino is in his minimal embedding, the first kachel HAS TO be in any column in the first row.
        int firstColIndex = tetrominoArray.getRowIndexOfFirstActiveKachel();

        ArrayList<MatrixCoordinate> relativeCoordinates = new ArrayList<>();
        getRelativePositionOfKacheln(tetrominoArray.getInternalArray(), 0, firstColIndex, 0, 0, relativeCoordinates);

        relativeCoordinates.remove(0); //remove first index as it is always our starting point at (0,0)

        return relativeCoordinates;
    }


    //TODO: javadoc

    /**
     * Determines the amount of kacheln that are connected to each other without any holes by using a recursive approach.
     *
     * @param internalArray            Array holding the kxk-Grid
     * @param start_row                The relative row that should be checked. Initialization value should be 0.
     * @param start_col                The relative row that should be checked. Initialization value should be 0.
     * @param relativeCoordinates      Holding a list of all found relativeCoordinates. Do provide an empty {@link ArrayList<>} at initialization.
     */
    private void getRelativePositionOfKacheln(int[][] internalArray, int start_row, int start_col, int relative_row, int relative_col, ArrayList<MatrixCoordinate> relativeCoordinates) {

        int row = start_row + relative_row;
        int col = start_col + relative_col;

        //break condition: We went outside the field, have found a cell with value 0 or have already counted that cell.
        if (row >= internalArray.length || row < 0
                || col >= internalArray[row].length || col < 0
                || internalArray[row][col] <= 0
                || positionAlreadyVisited(relative_row, relative_col, relativeCoordinates)) {
            return;
        }

        //if got here, we've found a new cell which is a neighbour of our previous cell.
        //Adding this cell to the list
        relativeCoordinates.add((new MatrixCoordinate(relative_row, relative_col)));


        //At every cell in the matrix we could go potentially in four different directions.

        //goRight
        getRelativePositionOfKacheln(internalArray, start_row, start_col, relative_row, relative_col + 1, relativeCoordinates);

        //goLeft
        getRelativePositionOfKacheln(internalArray, start_row, start_col, relative_row, relative_col - 1, relativeCoordinates);

        //goTop
        getRelativePositionOfKacheln(internalArray, start_row, start_col, relative_row - 1, relative_col, relativeCoordinates);

        //goBottom
        getRelativePositionOfKacheln(internalArray, start_row, start_col, relative_row + 1, relative_col, relativeCoordinates);


    }

    /**
     * Determines the amount of kacheln that are connected to each other without any holes.
     *
     * @param internalArray            Array holding the kxk-Grid
     * @param row                      The absolute row that should be checked. Initialization value should be the first row with an active kachel.
     * @param col                      The absolute row that should be checked. Initialization value should be the first column with an active kachel.
     * @param visitedMatrixCoordiantes Holding a list of all visited kacheln. Do provide an empty {@link ArrayList<>} at initialization.
     * @return The amount of kacheln that are connected to each other starting with the kachel set by {@code row and col}.
     */
    private int getTotalNumberOfConnectedKacheln(int[][] internalArray, int row, int col, ArrayList<MatrixCoordinate> visitedMatrixCoordiantes) {


        //break condition: We went outside the field, have found a cell with value 0 or have already counted that cell.
        if (row >= internalArray.length || row < 0
                || col >= internalArray[row].length || col < 0
                || internalArray[row][col] == 0
                || positionAlreadyVisited(row, col, visitedMatrixCoordiantes)) {
            return 0;
        }

        //if got here, we've found a new cell which is a neighbour of our start cell.
        //Adding this cell to the list of visited cells and adding +1 to the return statement below.
        visitedMatrixCoordiantes.add((new MatrixCoordinate(row, col)));


        //At every cell in the matrix we could go potentially in four different directions.

        //goRight
        int right = getTotalNumberOfConnectedKacheln(internalArray, row, col + 1, visitedMatrixCoordiantes);

        //goLeft
        int left = getTotalNumberOfConnectedKacheln(internalArray, row, col - 1, visitedMatrixCoordiantes);

        //goTop
        int top = getTotalNumberOfConnectedKacheln(internalArray, row - 1, col, visitedMatrixCoordiantes);

        //goBottom
        int bottom = getTotalNumberOfConnectedKacheln(internalArray, row + 1, col, visitedMatrixCoordiantes);

        //return number of neighbours found in right, left, top and bottom directions + 1 as the current cell is also a valid cell that counts.
        return right + left + top + bottom + 1;
    }

    /**
     * Checks whether the provided shape still has enough free place to hold another tetromino.
     *
     * @param tetrominoArray The tetrominoArray to be checked.
     * @return true, if another tetromino still can be fitted into that array
     */
    private boolean isEnoughPlaceForOtherTetromino(TetrominoArray tetrominoArray) {
        int index = tetrominoArray.getRowIndexOfFirstActiveKachel();
        int connectedKacheln = getTotalNumberOfConnectedKacheln(tetrominoArray.getInternalArray(), 0, index, new ArrayList<>());
        return connectedKacheln == tetrominoArray.getInternalArrayLength();
    }

    /**
     * Returns true if the given row and column were already included as {@link MatrixCoordinate} in the given ArrayList.
     * @param row The row to be checked
     * @param col The column to be checked
     * @param matrixCoordinates The ArrayList containing {@link MatrixCoordinate}s that are used.
     * @return True, if cell is already included in the ArrayList.
     */
    private boolean positionAlreadyVisited(int row, int col, ArrayList<MatrixCoordinate> matrixCoordinates) {
        for (MatrixCoordinate matrixCoordinate : matrixCoordinates) {
            if (matrixCoordinate.column == col && matrixCoordinate.row == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the grid length this component wants to have.
     * @param panelWidth The current panel's width of the component
     * @param panelHeight The current panel's height of the component
     * @return Grid length this component wants to have.
     */
    private int getGridLength(int panelWidth, int panelHeight) {
        Insets insets = getBorder().getBorderInsets(this);
        return Math.min(panelWidth - insets.left - insets.right - TETROMINO_PATH_STORKE, panelHeight - stringHeight - insets.bottom - insets.top - TETROMINO_PATH_STORKE); //shape should be quadratic
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        stringHeight = drawDescriptionID(new Point(getWidth() / 2, 0), "" + ID, g);

        Point startCoordinates = getCenteredStartPoint();
        shapeDraw.setStartCoordinates(startCoordinates);
        shapeDraw.draw(g);

    }

    /**
     * Returns a start point that centers the shape in the given space.
     * @return A Point that holds the coordinates of the upper left point where the shape matrix should start to appear centered on the screen.
     */
    private Point getCenteredStartPoint() {
        int[] bounds = shapeDraw.getBounds();
        int shapeWidth = bounds[2] - bounds[0];
        int shapeHeight = bounds[3] - bounds[1];

        Insets insets = getBorder().getBorderInsets(this);

        int unusedWidth = getWidth() - shapeWidth - insets.left - insets.right;
        int unusedHeight = getHeight() - shapeHeight - stringHeight - MARGIN_FROM_ID_DESCRIPTION_TO_SHAPE - insets.top - insets.bottom;


        int centeredX = (unusedWidth / 2) + insets.left + TETROMINO_PATH_STORKE;
        int centeredY = (unusedHeight / 2) + stringHeight + insets.top + TETROMINO_PATH_STORKE;
        Point startCoordinates = new Point(centeredX, centeredY);
        return startCoordinates;
    }


    /**
     * Returns the borders of the parking spot.
     * @return An int[4]-array with left, top, bottom, right coordinates of the borders.
     */
    public int[] getBorders() {
        int[] shapeBounds = shapeDraw.getBounds();
        return new int[]{shapeBounds[0], shapeBounds[1] - stringHeight, shapeBounds[2], shapeBounds[3]};
    }

    /**
     * This is a helper method which takes a string and draws it in the center.
     *
     * @param centerPoint The coordinates where the center of the string should be
     * @param message     The string to be displayed
     * @param g           The {@link Graphics} object to use
     * @return string's display height
     */
    private int drawDescriptionID(Point centerPoint, String message, Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 20));

        // get the FontMetrics for the current font
        FontMetrics fm = g.getFontMetrics();

        // get the length and height of string
        int stringWidth = fm.stringWidth(message);
        int stringHeight = fm.getAscent();

        // calculate the position for the first character
        int x = centerPoint.x - (stringWidth / 2);
        int y = centerPoint.y + stringHeight;

        Color color;
        if (isSelected) {
            color = Color.RED;
            g.setColor(color);

            //draw circle around number if selected
            int diameter = Math.max(stringWidth + MARGIN_AROUND_ID_DESCRIPTION, stringHeight + MARGIN_AROUND_ID_DESCRIPTION);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - diameter / 4, y - stringHeight, diameter, diameter);
        } else {
            color = Color.BLACK;
            g.setColor(color);
        }


        g.drawString(message, x, y);
        return stringHeight;
    }

    /**
     * Selects the Shape.
     */
    void select() {
        isSelected = true;
        setBorder(BorderFactory.createLoweredBevelBorder());
        repaint();
    }

    /**
     * Deselects the Shape.
     */
    void deselect() {
        isSelected = false;
        setBorder(BorderFactory.createRaisedBevelBorder());
        repaint();
    }

}
