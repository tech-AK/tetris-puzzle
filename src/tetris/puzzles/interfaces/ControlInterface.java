package tetris.puzzles.interfaces;

import tetris.puzzles.datamodels.UserPreferences;

/**
 * Provides an interface that the control components should implement.
 */
public interface ControlInterface {

    /**
     * If this method is invoked, the panel implementing this interface should show a warning sign to the user.
     */
    void displayWarning();

    /**
     * If this method is invoked, the panel implementing this interface should calculate, show and save the gotten points.
     * @param userPreferences The userPreferences used in the game when points were achieved
     */
    void addPoints(UserPreferences userPreferences);

    /**
     * If this method is invoked, the panel implementing this interface should save the user points into the database.
     * @param usedPreferences The userPreferences used in the game when points were achieved
     */
    void onGameOver(UserPreferences usedPreferences);
}
