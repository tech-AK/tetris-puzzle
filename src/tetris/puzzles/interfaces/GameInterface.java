package tetris.puzzles.interfaces;

import tetris.puzzles.datamodels.UserPreferences;

/**
 * Provides an interface that overall global game component should implement (i. e. panels that are responsible for starting the game).
 */
public interface GameInterface {

    /**
    Sets the user preferences selected in the "Vorauswahl" and starts the game.
    @param preferences The {@link UserPreferences} to use
     */
    void onGameStart(UserPreferences preferences);

    /**
     * Provides a method to set a link to a {@link ControlInterface}.
     * @param controlInterface The {@link ControlInterface} to be accessed by the implementing class.
     */
    void setControlInterface(ControlInterface controlInterface);

}
