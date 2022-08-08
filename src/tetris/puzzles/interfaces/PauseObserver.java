package tetris.puzzles.interfaces;

/**
 * Provides an interface that determines the implementation pause/continue options.
 */
public interface PauseObserver {

    /**
     * The implementing class should handle the pause of the game.
     */
    void pauseGame();

    /**
     * The implementing class should handle the continuing of the game.
     */
    void continueGame();
}
