package tetris.puzzles.control;

import tetris.tools.DatabaseSaver;
import tetris.puzzles.datamodels.FallingVelocity;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.ControlInterface;
import tetris.puzzles.interfaces.GameInterface;
import tetris.puzzles.interfaces.PauseObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import static tetris.puzzles.MainPuzzlesPanel.GAME_ID;

/**
 * This class is responsible for handling the right control screen where the user can choose his preferenches
 * and sees the dashboard.
 */
public class ControlPuzzlesPanel extends JPanel implements ControlInterface {

    JComboBox<Integer> numberOfKachelnInStoneMenu;
    JComboBox<Integer> numberOfNewAppearingStonesMenu;
    JComboBox<Integer> numberOfShapesMenu;
    JComboBox<Integer> amountOfColorsMenu;

    ButtonGroup fallingVelocityButtonGroup;
    JCheckBox increaseVelocity;

    DashboardPanel dashboard;

    GameInterface gameInterface;

    /**
     * Constructs the ControlPuzzlesPanel.
     * @param gameInterface A {@link GameInterface} in order to enable communication with the game panel (e. g. to start the game)
     * @param pauseObserver A {@link PauseObserver} in order to be able to pause the game
     */
    public ControlPuzzlesPanel(GameInterface gameInterface, PauseObserver pauseObserver) {
        this.gameInterface = gameInterface;
        gameInterface.setControlInterface(this);

        createOptionsLayout(pauseObserver);
    }

    /**
     * Creates the layout.
     * @param pauseObserver A {@link PauseObserver} in order to be able to pause the game
     */
    private void createOptionsLayout(PauseObserver pauseObserver) {
        setLayout(new GridBagLayout());

        Font fontTitle = new Font("Arial", Font.BOLD, 18);
        Font fontOptionDescription = new Font("Arial", Font.BOLD, 12);
        //Font fontHint = new Font("Arial", Font.ITALIC, 10);

        Integer[] numberOfKachelnInStoneOptions = new Integer[]{3, 4, 5};
        Integer[] numberOfNewAppearingStonesOptions = new Integer[]{1, 2, 3, 4, 5};
        Integer[] numberOfShapesOptions = new Integer[]{2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] amountOfColorsOptions = new Integer[]{1, 2, 3, 4, 5};

        dashboard = new DashboardPanel();
        add(dashboard, createConstraints(0, 0, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new int[]{10, 0, 0, 5}));
        dashboard.setVisible(false);

        JLabel titleOptions = new JLabel("Settings");
        underlineJLabel(titleOptions, fontTitle, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
        add(titleOptions, createConstraints(2, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new int[]{10, 0, 0, 0}));

        JLabel chooseVelocity = new JLabel("Speed");
        chooseVelocity.setFont(fontOptionDescription);
        underlineJLabel(chooseVelocity, fontOptionDescription, TextAttribute.UNDERLINE_LOW_GRAY);

        fallingVelocityButtonGroup = new ButtonGroup();
        JRadioButton slow = new JRadioButton("Slow (30 Sec.)");
        slow.setActionCommand(FallingVelocity.SLOW.name());
        JRadioButton medium = new JRadioButton("Medium (15 Sec.)");
        medium.setActionCommand(FallingVelocity.MEDIUM.name());
        JRadioButton fast = new JRadioButton("Fast (8 Sec.)");
        fast.setActionCommand(FallingVelocity.FAST.name());
        fallingVelocityButtonGroup.add(slow);
        fallingVelocityButtonGroup.add(medium);
        fallingVelocityButtonGroup.add(fast);
        medium.setSelected(true);

        JPanel panelVelocity = new JPanel();
        panelVelocity.setLayout(new BoxLayout(panelVelocity, BoxLayout.Y_AXIS));
        panelVelocity.add(chooseVelocity);
        panelVelocity.add(slow);
        panelVelocity.add(medium);
        panelVelocity.add(fast);

        add(panelVelocity, createConstraints(3, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.CENTER, new int[]{0, 0, 0, 10}));


        //Do not display the hint due to space limits
        //JLabel hintPanelSize = new JLabel("<HTML>Hinweis: Das Programm kann jederzeit <br> in eine beliebige Größe gezogen werden, <br> ohne dass diese Buttons benutzt werden müssen.</HTML>");
        //hintPanelSize.setFont(fontHint);
        //add(hintPanelSize, createConstraints(5,0,2,1,1,0, GridBagConstraints.PAGE_START, GridBagConstraints.NONE, new int[]{0,0,0,0}));

        increaseVelocity = new JCheckBox("Increase speed from time to time");
        add(increaseVelocity, createConstraints(4, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new int[]{0, 0, 0, 0}));


        numberOfKachelnInStoneMenu = addDropDownOption("Size of tetrominoes", numberOfKachelnInStoneOptions, 4, 5, fontOptionDescription);

        numberOfNewAppearingStonesMenu = addDropDownOption("No. of new tetrominoes", numberOfNewAppearingStonesOptions, 4, 6, fontOptionDescription);


        numberOfShapesMenu = addDropDownOption("No. tetromino shapes", numberOfShapesOptions, 8, 7, fontOptionDescription);


        amountOfColorsMenu = addDropDownOption("No. colours", amountOfColorsOptions, 4, 8, fontOptionDescription);


        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dashboard.setVisible(true);
                dashboard.resetCurrentPoints();
                setUserPreferences();
            }
        });
        start.setFocusable(false); //prevents the "selection-rectangle" to be drawn
        start.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                //draw a new start triangle
                Polygon triangle = new Polygon(new int[]{x - c.getWidth() / 10, x - c.getWidth() / 10, x},
                        new int[]{c.getHeight() / 5, c.getHeight() - c.getHeight() / 5, c.getHeight() / 2}, 3);

                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2));
                g.drawPolygon(triangle);

                g.setColor(Color.GREEN);
                g.fillPolygon(triangle);
            }

            @Override
            public int getIconWidth() {
                return 0;
            }

            @Override
            public int getIconHeight() {
                return 0;
            }
        });
        add(start, createConstraints(9, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new int[]{0, 0, 0, 0}));


        JButton instructions = new JButton("Instructions");
        instructions.setFocusable(false);
        instructions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PuzzlesInstructions(getCurrentUserPreferences());
                pauseObserver.pauseGame();
            }
        });
        add(instructions, createConstraints(9, 1, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new int[]{0, 0, 0, 5}));
    }

    /**
     * Returns the current UserPreferences.
     * <b>NOTE: This returns the UserPreferences as they are currently set up which could differ from the preferences currently used in the game.</b>
     * @return A {@link UserPreferences} object holding the user's preferences
     */
    private UserPreferences getCurrentUserPreferences() {
        UserPreferences userPreferences = new UserPreferences();

        userPreferences.setNumberOfShapes((Integer) numberOfShapesMenu.getSelectedItem());
        userPreferences.setNumberOfKachelnInTetromino((Integer) numberOfKachelnInStoneMenu.getSelectedItem());
        userPreferences.setNumberOfNewAppearingStones((Integer) numberOfNewAppearingStonesMenu.getSelectedItem());
        userPreferences.setAmountOfColors((Integer) amountOfColorsMenu.getSelectedItem());

        String velocitySelection = fallingVelocityButtonGroup.getSelection().getActionCommand();
        userPreferences.setVelocity(FallingVelocity.valueOf(velocitySelection));

        userPreferences.setVelocityIncreasing(increaseVelocity.isSelected());
        return userPreferences;
    }

    /**
     * Transmits the UserPreferences to the game panel and request to start the game.
     */
    private void setUserPreferences() {
        gameInterface.onGameStart(getCurrentUserPreferences());
    }

    /**
     * Convenience method to add a DropDown Menu to UI.
     * @param description The description of the DropDown Menu
     * @param options The options the DropDown Menu provides
     * @param defaultValueSelected The default value selected in DropDown Menu
     * @param row The row in layout the DropDown Menu should be positioned at
     * @param fontOptionDescription The font that should be used for DropDown Menu's description
     * @return A DropDown Menu as {@link JComboBox}
     */
    private JComboBox<Integer> addDropDownOption(String description, Integer[] options, int defaultValueSelected, int row, Font fontOptionDescription) {
        JLabel optionDescription = new JLabel(description);
        underlineJLabel(optionDescription, fontOptionDescription, TextAttribute.UNDERLINE_LOW_GRAY);
        add(optionDescription, createConstraints(row, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new int[]{0, 0, 0, 0}));

        JComboBox<Integer> dropDownMenu = new JComboBox<>(options);
        dropDownMenu.setSelectedItem(defaultValueSelected);
        add(dropDownMenu, createConstraints(row, 1, 1, 1, 1, 1, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new int[]{0, 10, 0, 10}));
        return dropDownMenu;
    }

    /**
     * Convenience method to underline a JLabel.
     * @param label The JLabel to be underlined
     * @param font The font to be used on the JLabel
     * @param textAttribute The text attributes that should be used on the JLabel
     */
    private void underlineJLabel(JLabel label, Font font, int textAttribute) {
        Map<TextAttribute, Integer> attributes = (Map<TextAttribute, Integer>) font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, textAttribute);
        label.setFont(font.deriveFont(attributes));
    }


    /**
     * Convenience method to add a new component to the GridBagLayout.  For more explanation see {@link GridBagLayout}.
     * @param row Row in the GridBagLayout
     * @param column Column in the GridBagLayout
     * @param width Width in the GridBagLayout
     * @param height Height in the GridBagLayout
     * @param weightx Weight on x axis in the GridBagLayout
     * @param weighty Weight on y axis in the GridBagLayout
     * @param anchor Anchor in the GridBagLayout
     * @param fill Fill mode in the GridBagLayout
     * @param padding Padding in the GridBagLayout
     * @return A {@link GridBagConstraints} that holds the specified requirements
     */
    private GridBagConstraints createConstraints(int row, int column, int width, int height, double weightx, double weighty, int anchor, int fill, int[] padding) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;  //row position in grid
        gbc.gridx = column; //column position in grid
        gbc.gridwidth = width; //how many cells does the component contain? Set gridwith = 2 to stretch the component over 2 cells
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = anchor;
        gbc.fill = fill;
        gbc.insets = new Insets(padding[0], padding[1], padding[2], padding[3]);
        return gbc;
    }

    @Override
    public void displayWarning() {
        dashboard.displayWarningSign();
    }

    @Override
    public void addPoints(UserPreferences usedPreferences) {
        //NOTE: We cannot use the getCurrentUserPreferences() here, as the user might has changed the preferences without relaunching the game.
        //In this case the user would get points calculated by UserPreferences that are selected, but not active in the game yet.
        dashboard.addPoints(calculatePoints(usedPreferences));
    }

    @Override
    public void onGameOver(UserPreferences usedPreferences) {
        if (usedPreferences.isDefaultPreferencesUsed()) {
            int points = dashboard.getCurrentPoints();
            new DatabaseSaver(GAME_ID, points);
        }
        dashboard.refresh();
    }


    /**
     * Method that calculates that points the user gets by using the current settings.
     * @param usedPreferences The current settings used in the game.
     * @return The score the user gets for fitting two tetrominoes into a shape.
     */
    private int calculatePoints(UserPreferences usedPreferences) {
        int sum = 0;

        switch (usedPreferences.getNumberOfKachelnInStone()) {
            case 3:
                sum += 5;
                break;
            case 4:
                sum += 10;
                break;
            case 5:
                sum += 20;
                break;
        }

        switch (usedPreferences.getNumberOfNewAppearingStones()) {
            case 1:
            case 2:
                sum += 0;
                break;
            case 3:
                sum += 5;
                break;
            case 4:
                sum += 10;
                break;
            case 5:
                sum += 15;
                break;
        }

        switch (usedPreferences.getNumberOfShapes()) {
            case 1:
                sum += 40;
                break;
            case 2:
                sum += 35;
                break;
            case 3:
                sum += 30;
                break;
            case 4:
                sum += 25;
                break;
            case 5:
                sum += 20;
                break;
            case 6:
                sum += 15;
                break;
            case 7:
                sum += 10;
                break;
            case 8:
                sum += 5;
                break;
            case 9:
                sum += 0;
                break;
        }

        switch (usedPreferences.getVelocity()) {
            case SLOW:
                sum += 0;
                break;
            case MEDIUM:
                sum += 5;
                break;
            case FAST:
                sum += 15;
                break;
        }

        if (usedPreferences.isVelocityIncreasing()) {
            sum += 10;
        }

        return sum;
    }
}
