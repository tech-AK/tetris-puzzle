package tetris.puzzles.control;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import static tetris.puzzles.game.GameGrid.REFRESH_RATE_IN_MILLIS;

/**
 * This class creates the InformationPanel showing warning sign or gotten points to the user.
 */
public class InformationPanel extends JPanel implements Runnable {

    Image img;
    float alpha = 0;
    int lastPoints;
    Color lastColor;

    Thread fadeAnimation;

    boolean showWarning;
    boolean showPoints;

    /**
     * Constructs the InformationPanel. Loads the warning image.
     */
    InformationPanel() {
        img = getLoadedImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showWarning) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        } else if (showPoints) {
            drawPointNotification(g);
        }

    }

    /**
     * Draws the notification of currently gotten points.
     * @param g Graphics object
     */
    private void drawPointNotification(Graphics g) {
        String displayString = "+" + lastPoints;

        g.setColor(new Color(lastColor.getRed(), lastColor.getGreen(), lastColor.getBlue(), (int) (alpha * 255)));
        g.setFont(new Font("Arial", Font.BOLD, determineMaxFontSize(displayString, getWidth(), getHeight(), g)));

        // get the FontMetrics for the current font
        FontMetrics fm = g.getFontMetrics();

        // get the width and height of string
        int stringWidth = fm.stringWidth(displayString);
        int stringHeight = fm.getAscent();

        // calculate the position for the leftmost character
        int x = (getWidth() / 2) - (stringWidth / 2);
        int y = (getHeight() / 2) + (stringHeight / 2);

        g.drawString(displayString, x, y);
    }

    /**
     * Determines the maximal possible font size for a given string for a given maxWidth and maxHeight.
     *
     * @param s         The string to be drawn on screen
     * @param maxWidth  The maximum width that should be fitted.
     * @param maxHeight The maximum height that should be fitted.
     * @param g         Graphics context
     * @return The biggest font size that still not exceed the given maximum maxHeight and maxWidth.
     * <b>NOTE: The font size is always in range: 18 <= fontsize <= 35.</b>
     */
    private int determineMaxFontSize(String s, int maxWidth, int maxHeight, Graphics g) {
        int fontSize = 18;

        for (; fontSize < 35; fontSize++) {
            g.setFont(new Font("Arial", Font.BOLD, fontSize));

            // get the FontMetrics for the current font
            FontMetrics fm = g.getFontMetrics();

            // get the length and maxHeight of string
            int stringWidth = fm.stringWidth(s);
            int stringHeight = fm.getAscent();

            if (stringWidth > maxWidth || stringHeight > maxHeight) {
                //font got to big, need to reduce again
                return fontSize - 1;
            }
        }

        //fontSize reached 35 and still fits into screen.
        return fontSize;

    }

    /**
     * Gets the warning sign as {@link BufferedImage}.
     *
     * @return BufferedImage containing the warning sign
     */
    private BufferedImage getLoadedImage() {
        ClassLoader classLoader = getClass().getClassLoader();


        try {
            return ImageIO.read(classLoader.getResource("resources/warning.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //something has gone wrong...
        return null;
    }

    /**
     * Starts a Fade-in and then Fade-out animation.
     */
    public void startFadeAnimation() {
        if (fadeAnimation != null && fadeAnimation.isAlive()) {
            fadeAnimation.interrupt();
        } else {
            fadeAnimation = new Thread(this);
            fadeAnimation.start();
        }

    }

    /**
     * Returns a random color of the following colors:
     * <br> Red, Green, Orange, Blue, Cyan, Magenta
     * @return A random color
     */
    private Color getRandomColor() {
        Color[] colorPalette = new Color[]{Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE, Color.CYAN, Color.MAGENTA};

        Random random = new Random();
        int randomIndex = random.nextInt(colorPalette.length);

        return colorPalette[randomIndex];
    }

    @Override
    public void run() {

        try {
            //fade in animation
            while (alpha <= 0.90) {
                alpha += 0.10;
                repaint();
                Thread.sleep(REFRESH_RATE_IN_MILLIS);
            }

            //show for 2 seconds
            Thread.sleep(2000);

            //fade out animation
            while (alpha >= 0.10) {
                alpha -= 0.10;
                repaint();
                Thread.sleep(REFRESH_RATE_IN_MILLIS);
            }
        } catch (InterruptedException e) {
            // Thread was asked to interrupt, most likely the class requested to display another animation
            // while the first animation still did not finished. In this case, just restart the animation.

            alpha = 0;
            fadeAnimation = new Thread(this);
            fadeAnimation.start();
        }

        //animation finished
    }

    /**
     * Prepares the panel to show the warning sign.
     */
    public void displaySign() {
        showWarning = true;
        showPoints = false;
        startFadeAnimation();
    }

    /**
     * Prepares the panel to show the gotten user points.
     */
    public void displayPoints(int point) {
        showPoints = true;
        showWarning = false;
        lastPoints = point;
        lastColor = getRandomColor();
        startFadeAnimation();
    }
}
