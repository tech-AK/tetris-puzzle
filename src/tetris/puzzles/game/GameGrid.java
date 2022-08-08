package tetris.puzzles.game;

import tetris.tools.Tetromino;
import tetris.tools.TetrominoArraylist;
import tetris.puzzles.actions.ActionMirror;
import tetris.puzzles.actions.ActionMove;
import tetris.puzzles.actions.ActionTurn;
import tetris.puzzles.datamodels.UserPreferences;
import tetris.puzzles.interfaces.SizeObserver;
import tetris.puzzles.interfaces.TetrominoObserver;
import tetris.puzzles.tetromino.TetrominoArray;
import tetris.puzzles.tetromino.TetrominoDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class is responsible for handling the game grid where the tetrominoes appear and are falling down.
 */
public class GameGrid extends JPanel {

    public static final int TIME_IN_MS_BETWEEN_VELOCITY_INCREASE = 30000; //30 sek
    public static final int TIME_IN_MS_BETWEEN_NEW_TETROMINOES_RELEASED = 10000; //10 sek
    public static final int REFRESH_RATE_IN_MILLIS = 40; //human eye is able to capture ~25 frame per second, so producing every 40ms a new frame.

    UserPreferences userPreferences;

    TetrominoDraw activeTetromino;

    Thread moveTetrominoes, createNewTetrominoes, increaseVelocity;
    Thread delayStart, creationDelay, movementDelay;

    int oldHeight;
    int newHeight;

    long missingTimeDifferenceTillNextCreation;
    long missingTimeDifferenceTillNextMovement;

    int alpha = 0;

    private float timeUntilOneTetrominoGetsToGroundInSek;
    private int translatedPixelsPerMovement;

    SizeObserver sizeObserver;
    TetrominoObserver tetrominoObserver;

    /**
     * Constructs a GameGrid object.
     * @param tetrominoObserver A {@link TetrominoObserver} that can be used to get selected tetrominoes
     * @param sizeObserver A {@link SizeObserver} that can be used to get the current size of tetrominoes
     * @param userPreferences A {@link UserPreferences} object that holds the user preferences that should be used in the game.
     */
    GameGrid(TetrominoObserver tetrominoObserver, SizeObserver sizeObserver, UserPreferences userPreferences) {
        this.tetrominoObserver = tetrominoObserver;
        this.sizeObserver = sizeObserver;
        this.userPreferences = userPreferences;

        setBackground(new Color(215, 215, 215)); //light gray

        switch (userPreferences.getVelocity()) {
            case FAST:
                timeUntilOneTetrominoGetsToGroundInSek = 8;
                translatedPixelsPerMovement = 1; //use the smallest int unit possible to get fluently movements
                break;
            case MEDIUM:
                timeUntilOneTetrominoGetsToGroundInSek = 15;
                translatedPixelsPerMovement = 1;
                break;
            case SLOW:
                timeUntilOneTetrominoGetsToGroundInSek = 30;
                translatedPixelsPerMovement = 1;
                break;
        }


        addClickListener();

        addPanelSizeListener();

        addKeyBindings();

        startGame();
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    /**
     * Adds a size listener to the panel that is triggered when window resizes.
     * Note: Everytime the windows resizes and the tetrominoes have to be replaced in the grid, the check for a game over is temporarily disabled.
     */
    private void addPanelSizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                if (getHeight() != newHeight) {
                    tetrominoObserver.disableGameOver(); //disable GameOver check until layout has redrawn and tetrominoes re-sized

                    oldHeight = newHeight;
                    newHeight = getHeight();

                    onGridSizeHasChanged();
                }

            }
        });
    }

    /**
     * Adds a MouseListener that determines if the user clicked on a tetromino and handles the disselect / select logic.
     */
    private void addClickListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                Point clicked = e.getPoint();
                TetrominoDraw tetromino = getTetrominoAtPoint(clicked);

                if (tetromino != null) {

                    if (activeTetromino != null) {
                        activeTetromino.setSelected(false);
                        if (activeTetromino == tetromino) {
                            //user tapped on the same tetromino, so need to deselect and nothing more
                            activeTetromino = null;
                            tetrominoObserver.setSelectedTetromino(null, true);
                            return;
                        }
                    }

                    tetromino.setSelected(true);
                    setEnableStateKeyListener(true);
                    repaint();
                    tetrominoObserver.setSelectedTetromino(tetromino, true);
                    activeTetromino = tetromino;
                }
            }
        });
    }


    /**
     * Returns the refresh time that a thread should implement so that
     * a tetromino gets in the specified time to ground by travelling the specified amount of pixels per each movement.
     * @param timeUntilOneTetrominoGetsToGroundInSek The time a tetromino should (roughly) take until it hits the game grid ground.
     * @param translatedPixelsPerMovement The amount of pixels the tetromino should travel per each movement
     * @return The time between two update calls of a thread in Milliseconds
     */
    private int getRefreshTimeInMillis(float timeUntilOneTetrominoGetsToGroundInSek, int translatedPixelsPerMovement) {
        //We want the tetromino to move 1 px each call of the Thread.
        //So calculate the time the thread needs to refresh himself

        // This logic first determines the available height and then calculates how many pixels per second
        // needs to be added in order to move through the whole panel after timeUntilOneTetrominoGetsToGroundInSek has passed.
        int maxTetrominoHeight = sizeObserver.getTetrominoKachelSize() * userPreferences.getNumberOfKachelnInStone();
        int pixelsToTravelTillDeath = (getHeight() - maxTetrominoHeight);

        int totalCallsTillDeath = pixelsToTravelTillDeath / translatedPixelsPerMovement;

        int timeBetweenTwoCallsInMillis = Math.round((timeUntilOneTetrominoGetsToGroundInSek * 1000) / totalCallsTillDeath);

        return timeBetweenTwoCallsInMillis;
    }


    /**
     * Starts the game.
     */
    public void startGame() {

        //Small delay before tetrominoes start to fall so that user can orientate himself and look at the given shapes.
        delayStart = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);


                    startConstantTetrominoCreation();
                    startConstantTetrominoMovement();
                    if (userPreferences.isVelocityIncreasing()) {
                        startVelocityIncreasing();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        delayStart.start();
    }

    /**
     * Starts a thread that enables the constant increasing of the velocity.
     */
    private void startVelocityIncreasing() {
        increaseVelocity = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();

                try {
                    while (!thread.isInterrupted()) {

                        Thread.sleep(TIME_IN_MS_BETWEEN_VELOCITY_INCREASE);

                        timeUntilOneTetrominoGetsToGroundInSek = Math.max(timeUntilOneTetrominoGetsToGroundInSek - 2, 3); //tetromino get 2 seconds faster to ground, but never less than 3 seconds
                    }
                } catch (InterruptedException e) {
                    //Thread was asked to interrupt. Just exit while loop and terminate.
                }
            }
        });
        increaseVelocity.start();
    }

    /**
     * Starts a thread that enables the constant creation of new tetrominoes.
     */
    private void startConstantTetrominoCreation() {
        createNewTetrominoes = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();

                long timeSinceLastCreation = 0;
                try {
                    while (!thread.isInterrupted()) {

                        timeSinceLastCreation = System.currentTimeMillis();
                        createNewTetromino();

                        Thread.sleep(TIME_IN_MS_BETWEEN_NEW_TETROMINOES_RELEASED);
                    }
                } catch (InterruptedException e) {
                    //Thread was asked to interrupt.

                    //We need some logic so that the game really pauses, i. e. the game saves how many time has passed by
                    //and thus, which time difference needs to be waited until new tetrominoes should appear.
                    if (timeSinceLastCreation == 0) {
                        //thread never could start to create tetrominoes even once. No time difference to wait.
                        missingTimeDifferenceTillNextCreation = 0;
                    } else {
                        long terminateTime = System.currentTimeMillis();
                        long alreadyPassedTime = terminateTime - timeSinceLastCreation;
                        missingTimeDifferenceTillNextCreation = TIME_IN_MS_BETWEEN_NEW_TETROMINOES_RELEASED - alreadyPassedTime;
                    }


                }
            }
        });
        createNewTetrominoes.start();
    }

    /**
     * Starts a thread that enables the constant movement of tetrominoes.
     */
    private void startConstantTetrominoMovement() {
        moveTetrominoes = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();

                long timeSinceLastMovement = 0;
                try {
                    while (!thread.isInterrupted()) {

                        for (int i = 0; ; i++) {
                            TetrominoDraw tetromino = tetrominoObserver.getTetrominoesInGameGridAtIndex(i);
                            if (tetromino == null) {
                                //reached end of list
                                break;
                            } else {
                                int[] borders = getBorders();
                                tetromino.translateInPx(0, translatedPixelsPerMovement, borders[3]);
                            }
                        }

                        repaint();
                        timeSinceLastMovement = System.currentTimeMillis();
                        Thread.sleep(getRefreshTimeInMillis(timeUntilOneTetrominoGetsToGroundInSek, translatedPixelsPerMovement));

                    }
                } catch (InterruptedException e) {
                    //We need some logic so that the game really pauses, i. e. the game saves how many time has passed by
                    //and thus, which time difference needs to be waited until new tetrominoes should appear.
                    if (timeSinceLastMovement == 0) {
                        //thread never could start to create tetrominoes even once. No time difference to wait.
                        missingTimeDifferenceTillNextMovement = 0;
                    } else {
                        long terminateTime = System.currentTimeMillis();
                        long alreadyPassedTime = terminateTime - timeSinceLastMovement;
                        missingTimeDifferenceTillNextMovement = getRefreshTimeInMillis(timeUntilOneTetrominoGetsToGroundInSek, translatedPixelsPerMovement) - alreadyPassedTime;
                    }
                }
            }
        });
        moveTetrominoes.start();
    }

    /**
     * Returns the borders of the game grid.
     * @return An int[4]-array with left, top, bottom, right coordinates of the borders.
     */
    public int[] getBorders() {
        return new int[]{2, 0, getWidth(), getHeight()}; //2px margin to left side
    }

    public TetrominoObserver getTetrominoObserver() {
        return tetrominoObserver;
    }

    /**
     * Adds KeyBindings to the game grid.
     */
    private void addKeyBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "move_tetromino_left");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "move_tetromino_left");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "move_tetromino_right");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "move_tetromino_right");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "move_tetromino_down");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "move_tetromino_down");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("C"), "turn_tetromino_right");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Y"), "turn_tetromino_left");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "mirror_tetromino_horizontal");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("E"), "mirror_tetromino_vertical");


        getActionMap().put("move_tetromino_left", new ActionMove(-1, 0));
        getActionMap().put("move_tetromino_right", new ActionMove(1, 0));
        getActionMap().put("move_tetromino_down", new ActionMove(0, 1));

        getActionMap().put("turn_tetromino_right", new ActionTurn(true));
        getActionMap().put("turn_tetromino_left", new ActionTurn(false));

        getActionMap().put("mirror_tetromino_horizontal", new ActionMirror(true));
        getActionMap().put("mirror_tetromino_vertical", new ActionMirror(false));

        setEnableStateKeyListener(false); //Key Bindings get only active when this component gets focused.
    }

    /**
     * Enables or disables all key bindings of this class.
     * @param stateKeyListener If true, all key bindings will be enabled. If false, all key bindings will be disabled.
     */
    public void setEnableStateKeyListener(boolean stateKeyListener) {
        Object[] keys = getActionMap().keys();
        for (Object key : keys) {
            Action action = getActionMap().get(key);
            action.setEnabled(stateKeyListener);
        }
    }

    /**
     * Removes the selection of the current active tetromino.
     */
    void removeTetrominoSelection() {
        if (activeTetromino != null) {
            activeTetromino.setSelected(false);
            setEnableStateKeyListener(false);
            activeTetromino = null;
        }
    }

    /**
     * Returns the tetromino at the given points. Could be null if no tetromino is at the given point.
     * @param clicked The given point where to search for a tetromino
     * @return A {@link TetrominoDraw} object which is at the given point. Null if no object found at the given point.
     */
    private TetrominoDraw getTetrominoAtPoint(Point clicked) {
        for (int i = 0; ; i++) {
            TetrominoDraw tetrominoDraw = tetrominoObserver.getTetrominoesInGameGridAtIndex(i);
            if (tetrominoDraw == null) {
                //reached end of list
                break;
            } else {
                if (tetrominoDraw.isPointInTetromino(clicked)) {
                    return tetrominoDraw;
                }
            }
        }

        return null;
    }

    //as we have no component added to the panel, we need to override the paint method (and not paintComponent)
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for (int i = 0; ; i++) {
            TetrominoDraw tetromino = tetrominoObserver.getTetrominoesInGameGridAtIndex(i);
            if (tetromino == null) {
                //reached end of list
                break;
            }
            tetromino.draw(g);
        }

    }


    /**
     * Creates and displays new Tetrominoes on the game grid.
     */
    private void createNewTetromino() {
        int kachelLength = sizeObserver.getTetrominoKachelSize();

        if (kachelLength < 0) {
            return; //our layout hasn't laid out yet
        }

        //FIRST: Create the tetrominoes
        TetrominoDraw[] newCreatedTetrominoes = new TetrominoDraw[userPreferences.getNumberOfNewAppearingStones()];

        int totalWidthForTetrominoes = 0;
        for (int i = 0; i < newCreatedTetrominoes.length; i++) {
            newCreatedTetrominoes[i] = new TetrominoDraw(getRandomTetrominoArray(userPreferences), tetrominoObserver, sizeObserver, userPreferences);

            int[] tetrominoBounds = newCreatedTetrominoes[i].getBounds();
            int length = tetrominoBounds[2] - tetrominoBounds[0];
            totalWidthForTetrominoes += length;
        }

        int totalSpaceBetweenTetrominoes = getWidth() - totalWidthForTetrominoes;
        int spaceBetweenTetrominoes = totalSpaceBetweenTetrominoes / (newCreatedTetrominoes.length + 1); //to the leftest and rightest tetromino, there should be space too
        int spaceInKacheln = spaceBetweenTetrominoes / kachelLength;


        //SECOND: Position the tetrominoes distributed on the screen
        int xPos = 0;
        for (int i = 0; i < newCreatedTetrominoes.length; i++) {

            xPos += spaceInKacheln * kachelLength;
            newCreatedTetrominoes[i].setStartCoordinates(new Point(xPos, 0));

            tetrominoObserver.addTetrominoToGameGridList(newCreatedTetrominoes[i]);

            int[] tetrominoBounds = newCreatedTetrominoes[i].getBounds();
            int lengthOfAddedTetromino = tetrominoBounds[2] - tetrominoBounds[0];
            xPos += lengthOfAddedTetromino;
        }

        repaint();

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
     * Returns the currently selected {@link TetrominoDraw} in the game grid (if any).
     * @return {@link TetrominoDraw} or null if no tetromino is active
     */
    public TetrominoDraw getActiveTetromino() {
        return activeTetromino;
    }

    /**
     * Handles logic to handle a game over event.
     */
    public void onGameOver() {
        onGamePaused();
    }

    /**
     * Handles logic to pause the game.
     */
    public void onGamePaused() {
        //ask Threads to stop

        if (moveTetrominoes != null && moveTetrominoes.isAlive()) {
            moveTetrominoes.interrupt();
        }

        if (createNewTetrominoes != null && createNewTetrominoes.isAlive()) {
            createNewTetrominoes.interrupt();
        }

        if (increaseVelocity != null && increaseVelocity.isAlive()) {
            increaseVelocity.interrupt();
        }

        if (creationDelay != null && creationDelay.isAlive()) {
            creationDelay.interrupt();
        }

        if (movementDelay != null && movementDelay.isAlive()) {
            movementDelay.interrupt();
        }

        if (delayStart != null && delayStart.isAlive()) {
            delayStart.interrupt();
        }
    }

    /**
     * Handles logic to continue a game after it was paused.
     */
    public void onGameContinued() {
        creationDelay = new Thread(new Runnable() {
            @Override
            public void run() {
                long startWaitingTime = System.currentTimeMillis();
                try {

                    if (missingTimeDifferenceTillNextCreation > 0) {
                        Thread.sleep(missingTimeDifferenceTillNextCreation);
                    }

                    startConstantTetrominoCreation();
                } catch (InterruptedException e) {
                    long interruptTime = System.currentTimeMillis();
                    long timeWaited = interruptTime - startWaitingTime;
                    missingTimeDifferenceTillNextCreation -= timeWaited;
                }
            }
        });
        creationDelay.start();

        movementDelay = new Thread(new Runnable() {
            @Override
            public void run() {
                long startWaitingTime = System.currentTimeMillis();
                try {

                    if (missingTimeDifferenceTillNextMovement > 0) {
                        Thread.sleep(missingTimeDifferenceTillNextMovement);
                    }

                    startConstantTetrominoMovement();
                } catch (InterruptedException e) {
                    long interruptTime = System.currentTimeMillis();
                    long timeWaited = interruptTime - startWaitingTime;
                    missingTimeDifferenceTillNextMovement -= timeWaited;
                }
            }
        });
        movementDelay.start();


        if (userPreferences.isVelocityIncreasing()) {
            startVelocityIncreasing();
        }
    }

    //NOTE: This method could be called multiple times.

    /**
     * Handles the size change of the game grid. It adapts the x position of tetrominoes if they got out of bounds and
     * also adapts the height of the tetrominoes for the new game grid size. The adaption takes place relatively:
     * Thus, a tetromino that have already traveled e. g. 70% of the game grid in the old size,
     * now also has traveled 70% of the new game grid size.
     * This method could be called multiple times sequentially.
     */
    public void onGridSizeHasChanged() {
        boolean atLeastOneTetrominoIsOutOfBounds = false;

        for (int i = 0; ; i++) {
            TetrominoDraw tetrominoDraw = tetrominoObserver.getTetrominoesInGameGridAtIndex(i);
            if (tetrominoDraw == null) {
                //reached end of list
                break;
            }

            int[] bounds = tetrominoDraw.getBounds();

            int[] gridGameBorders = getBorders();

            //reset x position if tetromino now outside the grid
            if (bounds[2] > gridGameBorders[2]) {
                tetrominoDraw.translateInPx(-(bounds[2] - gridGameBorders[2]), 0, gridGameBorders[3]);
            }

            //reset y position to adapt to new screen
            float traveledWayFraction = bounds[1] / (float) oldHeight;
            int newTraveledWay = (int) (newHeight * traveledWayFraction);
            int dy = newTraveledWay - bounds[1];
            tetrominoDraw.translateInPx(0, dy, gridGameBorders[3]);

            if (tetrominoDraw.isGameOver(gridGameBorders[3])) {
                atLeastOneTetrominoIsOutOfBounds = true;
            }
        }

        if (!atLeastOneTetrominoIsOutOfBounds) {
            //game layout is completely re-drawn, now we can enable GameOver checker again
            tetrominoObserver.enableGameOver();
        }
    }
}
