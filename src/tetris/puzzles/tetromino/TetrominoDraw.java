package tetris.puzzles.tetromino;

import tetris.puzzles.datamodels.Line;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.interfaces.TetrominoObserver;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * This class extends {@link TetrominoArray} and adds more functionality.
 * While {@link TetrominoArray} only handles the internal array manipulation,
 * this class is also responsible for drawing and positioning the tetromino on the screen.
 */
public class TetrominoDraw extends TetrominoArray {

    public static final int TETROMINO_PATH_STORKE = 2;
    Point startCoordinates;
    Point endCoordinates;
    Color[] colorArray = new Color[3];

    int alpha = 255;

    boolean isSelected;

    GeneralPath path;

    ArrayList<Point> shapePoints;

    SizeObserver sizeObserver;
    TetrominoObserver tetrominoObserver;

    public static final Color activeColor = Color.YELLOW;

    /**
     * Creates a new TetrominoDraw object that can be used to draw Tetrominoes on screen.
     * @param internalArray The internal array of the tetromino that should be used.
     * @param startCoordinates The upper left point where the matrix should start.
     * @param userPreferences  The {@link UserPreferences} in order to determine how many colors should be used.
     */
    public TetrominoDraw(int[][] internalArray, Point startCoordinates, TetrominoObserver tetrominoObserver, SizeObserver sizeObserver, UserPreferences userPreferences) {
        super(internalArray);
        this.startCoordinates = startCoordinates;
        this.tetrominoObserver = tetrominoObserver;
        this.sizeObserver = sizeObserver;

        if (userPreferences != null) {
            colorArray[0] = getRandomColor(userPreferences.getAmountOfColors());
        }


        shapePoints = new ArrayList<>();

    }

    public TetrominoDraw(int[][] internalArray, TetrominoObserver tetrominoObserver, SizeObserver sizeObserver, UserPreferences userPreferences) {
        this(internalArray, new Point(0, 0), tetrominoObserver, sizeObserver, userPreferences);
    }

    public TetrominoDraw(TetrominoArray tetrominoArray, TetrominoObserver tetrominoObserver, SizeObserver sizeObserver, UserPreferences userPreferences) {
        this(tetrominoArray.getInternalArray(), new Point(0, 0), tetrominoObserver, sizeObserver, userPreferences);
    }

    private Color getRandomColor(int colorAmount) {
        Color[] colorPalette = new Color[]{Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE, Color.CYAN};

        Random random = new Random();
        int randomIndex = random.nextInt(colorAmount);

        return colorPalette[randomIndex];
    }

    public void updateInternalArray(int[][] newArray) {
        this.internalArray = newArray;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setStartCoordinates(Point newCoordinates) {
        startCoordinates = newCoordinates;
    }

    public Point getStartCoordinates() {
        return startCoordinates;
    }

    /**
     * Tries to turn the tetromino (and resets if this is an invalid transaction)
     * @param turnRight If true, the tetromino is turned clockwise. Otherwise, turning counter-clockwise.
     * @param left The left border of the panel where the tetromino is embedded.
     * @param top The top border of the panel where the tetromino is embedded.
     * @param right The right border of the panel where the tetromino is embedded.
     * @param down The down border of the panel where the tetromino is embedded.
     * @param otherTetrominoesInGrid An ArrayList of other {@link TetrominoDraw} objects that are embedded in the same panel.
     */
    public void turn(boolean turnRight, int left, int top, int right, int down, ArrayList<TetrominoDraw> otherTetrominoesInGrid) {
        if (turnRight) {
            super.turnRight();
        } else {
            super.turnLeft();
        }

        if (isInvalidTransaction(left, top, right, down, otherTetrominoesInGrid)) {
            //reset transaction
            if (turnRight) {
                super.turnLeft();
            } else {
                super.turnRight();
            }
        }

    }

    /**
     * Tries to mirror the tetromino (and resets if this is an invalid transaction)
     * @param horizontally If true, the tetromino is mirrored horizontally. Otherwise, mirroring vertically.
     * @param left The left border of the panel where the tetromino is embedded.
     * @param top The top border of the panel where the tetromino is embedded.
     * @param right The right border of the panel where the tetromino is embedded.
     * @param down The down border of the panel where the tetromino is embedded.
     * @param otherTetrominosInGrid An ArrayList of other {@link TetrominoDraw} objects that are embedded in the same panel.
     */
    public void mirror(boolean horizontally, int left, int top, int right, int bottom, ArrayList<TetrominoDraw> otherTetrominosInGrid) {
        if (horizontally) {
            super.mirrorHorizontally();
        } else {
            super.mirrorVertically();
        }

        if (isInvalidTransaction(left, top, right, bottom, otherTetrominosInGrid)) {
            //reset transaction
            if (horizontally) {
                super.mirrorHorizontally();
            } else {
                super.mirrorVertically();
            }
        }
    }

    /**
     * Tries to move the tetromino. In contrast to {@link #translateInKachelUnitWithValidation(int, int, int, int, int, int, ArrayList, boolean)} this method
     * does not check whether the movement is valid (i. e. no borders are crossed).
     * @param dx Relative coordinates to move on the x axis.
     * @param dy Relative coordinates to move on the y axis.
     * @param bottom The bottom border of the panel where the tetromino is embedded.
     */
    public void translateInPx(float dx, float dy, int bottom) {
        startCoordinates.setLocation(startCoordinates.x + dx, startCoordinates.y + dy);
        checkForGameOver(bottom);
    }

    public void translateInKachelUnitWithValidation(int dx, int dy, int left, int top, int right, int bottom, ArrayList<TetrominoDraw> otherTetrominosInGrid, boolean checkForGameOver) {
        int kachelLength = sizeObserver.getTetrominoKachelSize();
        translateInPxWithValidation(dx * kachelLength, dy * kachelLength, left, top, right, bottom, otherTetrominosInGrid, checkForGameOver);
    }

    /**
     * Tries to move the tetromino (and resets if this is an invalid transaction)
     * @param dx Relative coordinates to move on the x axis.
     * @param dy Relative coordinates to move on the y axis.
     * @param left The left border of the panel where the tetromino is embedded.
     * @param top The top border of the panel where the tetromino is embedded.
     * @param right The right border of the panel where the tetromino is embedded.
     * @param bottom The bottom border of the panel where the tetromino is embedded.
     * @param otherTetrominosInGrid An ArrayList of other {@link TetrominoDraw} objects that are embedded in the same panel.
     */
    public void translateInPxWithValidation(int dx, int dy, int left, int top, int right, int bottom, ArrayList<TetrominoDraw> otherTetrominosInGrid, boolean checkForGameOver) {
        startCoordinates.setLocation(startCoordinates.x + dx, startCoordinates.y + dy);

        if (isInvalidTransaction(left, top, right, bottom, otherTetrominosInGrid)) {
            //reset transaction
            startCoordinates.setLocation(startCoordinates.x - dx, startCoordinates.y - dy);
        }

        if (checkForGameOver) {
            checkForGameOver(bottom);
        }
    }

    /**
     * Checks if game over occurred.
     * @param bottomBorder The bottom border of the panel where the tetromino is embedded.
     */
    private void checkForGameOver(int bottomBorder) {
        if (isGameOver(bottomBorder)) {
            tetrominoObserver.onGameOver();
        }
    }

    /**
     * Returns true, if tetromino hit the ground.
     * @param bottomBorder The bottom border of the panel where the tetromino is embedded.
     * @return True, if tetromino hit the ground. False otherwise.
     */
    public boolean isGameOver(int bottomBorder) {
        return endCoordinates.y > bottomBorder && bottomBorder > 0;
    }

    /**
     * Checks whether the transaction (moving/turning/mirroring tetromino) was invalid.
     * @param left The left border of the panel where the tetromino is embedded.
     * @param top The top border of the panel where the tetromino is embedded.
     * @param right The right border of the panel where the tetromino is embedded.
     * @param bottom The bottom border of the panel where the tetromino is embedded.
     * @param otherTetrominosInGrid An ArrayList of other {@link TetrominoDraw} objects that are embedded in the same panel.
     * @return True, if transaction was invalid, i. e. borders are crossed.
     */
    private boolean isInvalidTransaction(int left, int top, int right, int bottom, ArrayList<TetrominoDraw> otherTetrominosInGrid) {
        draw(null); //update internal rendering (endCoordinates and path)
        return (isOutOfBorder(left, top, right, bottom, endCoordinates) || isCollidingWithOtherTetromino(otherTetrominosInGrid));
    }

    private boolean isOutOfBorder(int left, int top, int right, int bottom, Point endCoordinates) {
        return (startCoordinates.x < left || endCoordinates.x + TETROMINO_PATH_STORKE > right
                || startCoordinates.y < top || endCoordinates.y + TETROMINO_PATH_STORKE > bottom);
    }

    /**
     * Returns true, if tetromino collides with another tetromino in grid.
     * @param otherTetrominosInGrid An ArrayList of other {@link TetrominoDraw} objects that are embedded in the same panel.
     * @return True, if tetromino collides with another tetromino in grid.
     */
    private boolean isCollidingWithOtherTetromino(ArrayList<TetrominoDraw> otherTetrominosInGrid) {
        if (otherTetrominosInGrid == null) {
            return false; //Skip validation if no other tetrominoes in grid
        }


        Area myArea = this.getBiggerTetrominoArea();
        boolean invalidTransaction = false;
        for (TetrominoDraw tetrominoDraw : otherTetrominosInGrid) {
            if (tetrominoDraw == this) {
                continue;
            }
            Area tetrominoArea = tetrominoDraw.getTetrominoArea();
            tetrominoArea.intersect(myArea);
            if (!tetrominoArea.isEmpty()) {
                //our tetrominoes would crash as their areas intersect - reset transaction
                invalidTransaction = true;
                break;
            }
        }

        return invalidTransaction;
    }

    /**
     * This method draws the tetromino using the provided Graphics object.
     * <br>NOTE: This method can also be called with a null Graphics object
     * causing the internal rendering of the tetromino to update, but not displaying in onto the screen. This can be used
     * to get the potential new coordinates of the tetromino and check the new coordinates BEFORE drawing the new rendering on the screen.
     * @param g Graphics object to use in order to paint Tetromino on screen or null if only updating the internal rendering.
     */
    public void draw(Graphics g) {
        ArrayList<Line> shapeLines = new ArrayList<>();

        int kachelLength = sizeObserver.getTetrominoKachelSize();

        int maxPositionX = 0;
        int maxPositionY = 0;

        // go through the tetromino's matrix and paint a square if the value is bigger than 0
        for (int row = 0; row < internalArray.length; row++) {
            for (int column = 0; column < internalArray[row].length; column++) {
                int xCoordinate = startCoordinates.x + column * kachelLength;
                int yCoordinate = startCoordinates.y + row * kachelLength;

                int kachelValue = internalArray[row][column];
                if (kachelValue > 0) {
                    if (g != null) {
                        if (isSelected) {
                            g.setColor(getAnimationColor(activeColor));
                        } else {
                            g.setColor(getAnimationColor(colorArray[kachelValue - 1]));
                        }
                        g.fillRect(xCoordinate, yCoordinate, kachelLength, kachelLength);
                    }


                    shapeLines.addAll(calculateShapeLines(internalArray, row, column, xCoordinate, yCoordinate, kachelValue, kachelLength));

                    //Update position
                    maxPositionX = Math.max(maxPositionX, xCoordinate + kachelLength);
                    maxPositionY = Math.max(maxPositionY, yCoordinate + kachelLength);
                }
            }
        }

        path = getPathOutOfLines(shapeLines);
        if (g != null) {
            //Draw shape of tetromino
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(TETROMINO_PATH_STORKE));
            g2.setColor(Color.BLACK);
            g2.draw(path);
        }


        endCoordinates = new Point(maxPositionX, maxPositionY);
    }

    public Color getAnimationColor(Color color) {
        return color; //animation is not used in TetrominoDraw, only in it subclass ShapeDraw.
    }

    public Color getTetrominoColor() {
        return colorArray[0];
    }

    public int[] getBounds() {
        draw(null); //update internal rendering first
        return new int[]{startCoordinates.x - TETROMINO_PATH_STORKE, startCoordinates.y - TETROMINO_PATH_STORKE, endCoordinates.x + TETROMINO_PATH_STORKE, endCoordinates.y + TETROMINO_PATH_STORKE};
    }

    /**
     * Returns a {@link GeneralPath} that contains an CLOSED path of a polygon, that can be used to draw the outer shape of a tetromino and create an area object.
     * @param shapeLines An ArrayList to use to obtain the right path.
     * @return A {@link GeneralPath} that contains an CLOSED path of a polygon containing all provided lines.
     */
    public GeneralPath getPathOutOfLines(ArrayList<Line> shapeLines) {
        Collections.sort(shapeLines);

        GeneralPath path = new GeneralPath();

        Line startLine = shapeLines.get(0);

        path.moveTo(startLine.x0, startLine.y0);
        path.lineTo(startLine.x1, startLine.y1);
        recursivePathFinder(path, shapeLines, startLine.x1, startLine.y1);

        //need to close path at the end, so that this path can be recognized as closed shape.
        //It does not change anything on the outer appearance of the shape as the recursivePathFinder algorithms automatically connects all points.
        path.closePath();
        return path;
    }

    /**
     * This method uses a recursive approach to find a CLOSED path with the given ArrayList of lines.
     * @param path GeneralPath that should be added to. The path needs to be already started by at least {@link GeneralPath#moveTo(float, float)}
     * @param shapeLines An ArrayList to use to obtain the right path.
     * @param lastX The coordinate on the x axis where the last line ended. Should be at initialization the current coordinates as set in {@link GeneralPath#moveTo(float, float)}
     * @param lastY The coordinate on the y axis where the last line ended. Should be at initialization the current coordinates as set in {@link GeneralPath#moveTo(float, float)}
     */
    private void recursivePathFinder(GeneralPath path, ArrayList<Line> shapeLines, int lastX, int lastY) {

        if (shapeLines.size() == 0) {
            return;
        }

        for (Line line : shapeLines) {
            if (line.x0 == lastX && line.y0 == lastY) {
                //in case the new line starts where the previous line ends
                path.lineTo(line.x1, line.y1);
                ArrayList<Line> reducedShapeLinesSet = new ArrayList<>(shapeLines);
                reducedShapeLinesSet.remove(line);
                recursivePathFinder(path, reducedShapeLinesSet, line.x1, line.y1);
                break;
            } else if (line.x1 == lastX && line.y1 == lastY) {
                //in case the line ends where the previous line ends
                path.lineTo(line.x0, line.y0);
                ArrayList<Line> reducedShapeLinesSet = new ArrayList<>(shapeLines);
                reducedShapeLinesSet.remove(line);
                recursivePathFinder(path, reducedShapeLinesSet, line.x0, line.y0);
                break;
            }
        }
    }

    /**
     * Returns an ArrayList with Lines that cover the outer shape of the Tetromino.
     * @param array The internal array of the tetromino.
     * @param row The row to be checked.
     * @param column The column to be checked.
     * @param xCoordinate The upper left coordinate on x axis of the current cell.
     * @param yCoordinate The upper left coordinate on y axis of the current cell.
     * @param kachelValue The value of the cell of the checked cell.
     * @param kachelLength The length of a kachel
     * @return An ArrayList with Lines that cover the outer shape of the Tetromino.
     */
    private ArrayList<Line> calculateShapeLines(int[][] array, int row, int column, int xCoordinate, int yCoordinate, int kachelValue, int kachelLength) {
        ArrayList<Line> lines = new ArrayList<>();

        // Draw left lines indicating the start of the tetromino if
        // and only if the kachel has no other active kachel in the same color (i. e. with the same kachelValue) to its left
        if (column - 1 < 0 || array[row][column - 1] != kachelValue) {
            lines.add(new Line(xCoordinate, yCoordinate, xCoordinate, yCoordinate + kachelLength));
        }

        //draw right lines
        if (column + 1 >= array[row].length || array[row][column + 1] != kachelValue) {
            lines.add(new Line(xCoordinate + kachelLength, yCoordinate, xCoordinate + kachelLength, yCoordinate + kachelLength));
        }

        //draw top lines
        if (row - 1 < 0 || array[row - 1][column] != kachelValue) {
            lines.add(new Line(xCoordinate, yCoordinate, xCoordinate + kachelLength, yCoordinate));
        }

        //draw bottom lines
        if (row + 1 >= array.length || array[row + 1][column] != kachelValue) {
            lines.add(new Line(xCoordinate, yCoordinate + kachelLength, xCoordinate + kachelLength, yCoordinate + kachelLength));
        }

        return lines;
    }

    /**
     * Returns an Area object that contains the area of this tetromino.
     * @return Tetrominoes area
     */
    public Area getTetrominoArea() {
        return new Area(path);
    }

    /**
     * Returns an Area object that contains the area of this tetromino and additionally one kachel length space to each side of the area.
     * @return Tetrominoes area with respect to one kachel space to each side of the area.
     */
    public Area getBiggerTetrominoArea() {
        int[][] biggerInternalArray = getBiggerInternalArray();

        ArrayList<Line> shapeLines = getShapeLinesForBiggerArray(biggerInternalArray);

        GeneralPath path = getPathOutOfLines(shapeLines);

        return new Area(path);
    }


    /**
     * Creates a bigger internal array that contains next to each positive value in the matrix another cell with the value 1.
     * This is useful for determine an array that holds the tetromino itself and plus one kachel size space to each side of the tetromino.
     * @return bigger int[][] array
     */
    private int[][] getBiggerInternalArray() {
        int[][] biggerInternalArray = new int[internalArray.length + 2][internalArray.length + 2]; //need a matrix that is bigger on both sides (left and right / top and down) (so +2)

        for (int row = 0; row < internalArray.length; row++) {
            for (int column = 0; column < internalArray[row].length; column++) {
                if (internalArray[row][column] > 0) {
                    int newArrayRow = row + 1;
                    int newArrayColumn = column + 1;
                    biggerInternalArray[newArrayRow][newArrayColumn] = 1;
                    biggerInternalArray[newArrayRow - 1][newArrayColumn] = 1;
                    biggerInternalArray[newArrayRow + 1][newArrayColumn] = 1;
                    biggerInternalArray[newArrayRow][newArrayColumn + 1] = 1;
                    biggerInternalArray[newArrayRow][newArrayColumn - 1] = 1;
                    biggerInternalArray[newArrayRow - 1][newArrayColumn - 1] = 1;
                    biggerInternalArray[newArrayRow + 1][newArrayColumn - 1] = 1;
                    biggerInternalArray[newArrayRow - 1][newArrayColumn + 1] = 1;
                    biggerInternalArray[newArrayRow + 1][newArrayColumn + 1] = 1;
                }
            }
        }
        return biggerInternalArray;
    }


    private ArrayList<Line> getShapeLinesForBiggerArray(int[][] biggerInternalArray) {
        int kachelLength = sizeObserver.getTetrominoKachelSize();
        Point startForNewBiggerArray = new Point(startCoordinates.x - kachelLength, startCoordinates.y - kachelLength);

        ArrayList<Line> shapeLines = new ArrayList<>();
        for (int row = 0; row < biggerInternalArray.length; row++) {
            for (int col = 0; col < biggerInternalArray[row].length; col++) {
                int xCoordinate = startForNewBiggerArray.x + col * kachelLength;
                int yCoordinate = startForNewBiggerArray.y + row * kachelLength;

                int kachelValue = biggerInternalArray[row][col];
                if (kachelValue > 0) {
                    shapeLines.addAll(calculateShapeLines(biggerInternalArray, row, col, xCoordinate, yCoordinate, kachelValue, kachelLength));
                }

            }
        }
        return shapeLines;
    }

    /**
     * Checks if the given point is in the Tetromino.
     * @param p The given point
     * @return True, if the given point is in the Tetromino.
     */
    public boolean isPointInTetromino(Point p) {
        int relativeX = p.x - startCoordinates.x;
        int relativeY = p.y - startCoordinates.y;

        if (relativeX < 0 || relativeY < 0) {
            //user has clicked above or left to tetromino's matrix. So user cannot have clicked the tetromino.
            return false;
        }

        int kachelSize = sizeObserver.getTetrominoKachelSize();

        int rowToCheck = relativeY / kachelSize;
        int columnToCheck = relativeX / kachelSize;


        if (rowToCheck < internalArray.length && columnToCheck < internalArray[rowToCheck].length) {
            //user has clicked into the tetromino's matrix. Need to check if the cell is equal to 1.
            return internalArray[rowToCheck][columnToCheck] == 1;
        } else {
            //user clicked much more right or below the tetromino so that the click does even not fit tetromino's matrix.
            return false;
        }
    }

}

