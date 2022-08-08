package tetris.puzzles.datamodels;

/**
 * This class holds a data model for saving the user preferences for the puzzles game.
 */
public class UserPreferences {

    private int numberOfKachelnInStone;
    private int numberOfParkingSpots;

    private int numberOfNewAppearingStones;

    private int numberOfShapes;

    private int amountOfColors;

    private FallingVelocity velocity;

    private boolean velocityIncreasing;


    /**
     * Checks whether the current preferences used are the default preferences.
     * @return True, if default preferences are used
     */
    public boolean isDefaultPreferencesUsed() {
        return (!isVelocityIncreasing()
                && getVelocity() == FallingVelocity.MEDIUM
                && getNumberOfKachelnInStone() == 4
                && getNumberOfNewAppearingStones() == 4
                && getNumberOfShapes() == 8
                && getAmountOfColors() == 4);
    }


    public int getAmountOfColors() {
        return amountOfColors;
    }

    public int getNumberOfKachelnInStone() {
        return numberOfKachelnInStone;
    }

    public int getNumberOfParkingSpots() {
        return numberOfParkingSpots;
    }

    public int getNumberOfNewAppearingStones() {
        return numberOfNewAppearingStones;
    }

    public FallingVelocity getVelocity() {
        return velocity;
    }

    public boolean isVelocityIncreasing() {
        return velocityIncreasing;
    }

    public int getNumberOfShapes() {
        return numberOfShapes;
    }


    public void setAmountOfColors(int amountOfColors) {
        this.amountOfColors = amountOfColors;
    }

    /**
     * Sets the number of kachel in tetromino (a. k. a. the grid size).
     * <b>Note that this setting automatically determines the number of parking spots.</b>
     * @param nr The number of kachel in tetromino (a. k. a. the grid size).
     */
    public void setNumberOfKachelnInTetromino(int nr) {
        numberOfKachelnInStone = nr;

        switch (numberOfKachelnInStone) {
            case 3:
                numberOfParkingSpots = 2;
                break;
            case 4:
                numberOfParkingSpots = 4;
                break;
            case 5:
                numberOfParkingSpots = 9;
                break;

        }
    }

    public void setNumberOfNewAppearingStones(int numberOfNewAppearingStones) {
        this.numberOfNewAppearingStones = numberOfNewAppearingStones;
    }

    public void setNumberOfShapes(int numberOfShapes) {
        this.numberOfShapes = numberOfShapes;
    }

    public void setVelocity(FallingVelocity velocity) {
        this.velocity = velocity;
    }

    public void setVelocityIncreasing(boolean velocityIncreasing) {
        this.velocityIncreasing = velocityIncreasing;
    }

}
