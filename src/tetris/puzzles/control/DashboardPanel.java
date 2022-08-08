package tetris.puzzles.control;

import tetris.tools.DatabaseSaver;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static tetris.puzzles.MainPuzzlesPanel.GAME_ID;

/**
 * This class creates the DashboardPanel showing meta information to the user during the game.
 */
public class DashboardPanel extends JPanel {

    JLabel currentScoreLabel;
    int currentPoints = 0;

    InformationPanel informationPanel;

    /**
     * Constructs a {@link DashboardPanel} object and creates the layout.
     */
    DashboardPanel() {
        createLayout();
    }

    /**
     * Creates the layout.
     */
    private void createLayout() {
        Border blackline = BorderFactory.createLineBorder(Color.black);
        TitledBorder title = BorderFactory.createTitledBorder(blackline, "Dashboard");
        title.setTitleJustification(TitledBorder.CENTER);
        setBorder(title);

        setLayout(new GridBagLayout());

        Font fontScore = new Font("Arial", Font.PLAIN, 15);

        currentScoreLabel = new JLabel("Current points: " + currentPoints);
        //currentScore.setHorizontalAlignment(SwingConstants.CENTER);
        currentScoreLabel.setFont(fontScore);

        GridBagConstraints currentScore_c = new GridBagConstraints();
        currentScore_c.gridx = 1;
        currentScore_c.gridy = 0;
        currentScore_c.gridheight = 1;
        currentScore_c.weightx = 0;
        currentScore_c.fill = GridBagConstraints.BOTH;

        add(currentScoreLabel, currentScore_c);


        JLabel highScore = new JLabel("Current highscore: " + getHighscorePoints());
        //highScore.setHorizontalAlignment(SwingConstants.CENTER);
        highScore.setFont(fontScore);

        GridBagConstraints highScore_c = new GridBagConstraints();
        highScore_c.gridx = 1;
        highScore_c.gridy = 1;
        highScore_c.gridheight = 1;
        highScore_c.weightx = 0;
        highScore_c.fill = GridBagConstraints.BOTH;

        add(highScore, highScore_c);


        JLabel highScoreName = new JLabel("achieved by " + getHighscoreName());
        highScoreName.setHorizontalAlignment(SwingConstants.RIGHT);
        highScoreName.setFont(fontScore);
        highScoreName.setFont(fontScore);

        GridBagConstraints highScore_name_c = new GridBagConstraints();
        highScore_name_c.gridx = 1;
        highScore_name_c.gridy = 2;
        highScore_name_c.gridheight = 1;
        highScore_name_c.weightx = 0;
        highScore_name_c.fill = GridBagConstraints.BOTH;

        add(highScoreName, highScore_name_c);

        /*
        Alternatively, we could use an ImageIcon but then we are not that flexible with adjusting the size and transparency of picture.
        ImageIcon im = new ImageIcon(this.getClass().getResource("/resources/warning.png"));
        JLabel label = new JLabel(im);
        add(label);
        */

        informationPanel = new InformationPanel();
        GridBagConstraints image_c = new GridBagConstraints();
        image_c.gridx = 0;
        image_c.gridy = 0;
        image_c.gridheight = 4;
        image_c.weightx = 1;
        image_c.fill = GridBagConstraints.BOTH;
        image_c.insets = new Insets(0, 0, 0, 10);

        add(informationPanel, image_c);

        revalidate();
        repaint();
    }

    /**
     * Returns the highscore points saved in the database.
     * @return Current highscore points saved in the database
     */
    private String getHighscorePoints() {
        DatabaseSaver databaseSaver = new DatabaseSaver();
        int points = databaseSaver.getPoints(GAME_ID);
        if (points < 0) {
            return "Ø"; //this is a scandinavian letter which simulates the mathematical symbol for empty set
        } else {
            return "" + points;
        }

    }

    /**
     * Returns the name corresponding to the highscore points returned by {@link #getHighscorePoints()}.
     * @return Name saved in the database which corresponds to the highscore points
     */
    private String getHighscoreName() {
        DatabaseSaver databaseSaver = new DatabaseSaver();
        String name = databaseSaver.getName(GAME_ID);
        if (name == null || name.equals("")) {
            return "-";
        } else {
            return name;
        }
    }

    /**
     * Refreshes the whole layout.
     */
    public void refresh() {
        removeAll();
        createLayout();
    }

    /**
     * Shows a warning sign to the user.
     */
    public void displayWarningSign() {
        informationPanel.displaySign();
    }

    /**
     * Add points the current score of the user.
     * @param points The amount of points that should be added
     */
    public void addPoints(int points) {
        //display new gotten points on dashboard
        informationPanel.displayPoints(points);

        //add points to score
        currentPoints += points;
        currentScoreLabel.setText("Current Points: " + currentPoints);
        repaint();
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    /**
     * Resets the current points back to zero.
     */
    public void resetCurrentPoints() {
        currentPoints = 0;
    }

}
