package tetris.puzzles;

import tetris.puzzles.control.ControlPuzzlesPanel;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.game.GamePuzzlesPanel;
import tetris.puzzles.interfaces.PauseObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


/**
 * This class is responsible for handling the whole layout for the Puzzles game.
 */
public class MainPuzzlesPanel extends JPanel implements PauseObserver {

    public static final int GAME_ID = 1;

    boolean gamePaused;
    JLabel pauseLabel;

    GamePuzzlesPanel game;
    ControlPuzzlesPanel control;

    Action gameExitPressed;
    Action pauseKeyPressed;

    public MainPuzzlesPanel() {
        setLayout(new GridBagLayout());


        GridBagConstraints game_c = new GridBagConstraints();
        GridBagConstraints control_c = new GridBagConstraints();

        game = new GamePuzzlesPanel(this);
        control = new ControlPuzzlesPanel(game, this);

        game_c.weightx = 1;
        game_c.weighty = 1;
        game_c.gridy = 0;
        game_c.gridx = 0;
        game_c.fill = GridBagConstraints.BOTH;


        control_c.weightx = 0;
        control_c.weighty = 1;
        control_c.gridy = 0;
        control_c.gridx = 1;
        control_c.fill = GridBagConstraints.BOTH;

        add(game, game_c);
        add(control, control_c);

        addKeyBindings();
    }

    /**
     * Creates Key Bindings.
     */
    private void addKeyBindings() {
        createKeyBindingActions();

        /*
         * Using Key Bindings as KeyListener only work if panel is focusable and uses requestFocus() / requestFocusInWindow()
         * However Pause function should always work (no matter whether ParkingSpots are selected etc.), so we're using gloabl Key Bindings
         * which will be active for the whole application and are more flexible.
         */
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("P"), "P_pressed");
        getActionMap().put("P_pressed", pauseKeyPressed);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Z"), "Z_pressed");
        getActionMap().put("Z_pressed", gameExitPressed);

        gameExitPressed.setEnabled(true);
        pauseKeyPressed.setEnabled(true);
    }

    private void createKeyBindingActions() {
        pauseKeyPressed = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gamePaused) {
                    continueGame();
                } else {
                    pauseGame();
                }

            }
        };

        gameExitPressed = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gamePaused) {
                    exitGame();
                }

            }
        };
    }

    /**
     * Exists the game.
     */
    private void exitGame() {
        //handle highscore saving
        UserPreferences userPreferences = game.getUserPreferences();
        if (userPreferences != null) {
            control.onGameOver(userPreferences);
        }

        System.exit(0);
    }

    @Override
    public void pauseGame() {

        gamePaused = true;
        game.setVisible(false);
        game.pauseGame();

        String pauseText = "Das Spiel wurde pausiert." +
                "<br>Drücken Sie erneut P, um weiterzuspielen" +
                "<br>oder Z, um zu beenden";
        pauseLabel = new JLabel("<html><div style='text-align: center;'>" + pauseText + "</div></html>");
        pauseLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        pauseLabel.setHorizontalAlignment(JLabel.CENTER);

        GridBagConstraints gbc_pauseLabel = new GridBagConstraints();
        gbc_pauseLabel.weightx = 0.7;
        gbc_pauseLabel.weighty = 1;
        //important to set pauseLabel at the same position where game layout was previously (game-layout gets collapsed by setVisible(false))
        gbc_pauseLabel.gridy = 0;
        gbc_pauseLabel.gridx = 0;
        gbc_pauseLabel.fill = GridBagConstraints.BOTH; //Stretch component horizontally and vertically

        add(pauseLabel, gbc_pauseLabel);
    }

    @Override
    public void continueGame() {
        gamePaused = false;

        if (pauseLabel != null) {
            remove(pauseLabel);
        }
        game.setVisible(true);

        game.continueGame();
    }

}
