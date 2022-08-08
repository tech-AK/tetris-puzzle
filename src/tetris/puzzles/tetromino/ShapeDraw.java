package tetris.puzzles.tetromino;

import tetris.puzzles.datamodels.Line;
import tetris.puzzles.game.Shape;
import tetris.puzzles.interfaces.SizeObserver;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

/**
 * This class extends {@link TetrominoDraw} and adds more functionality.
 * While {@link TetrominoDraw} only handles the drawing of usual tetrominoes,
 * this class is also responsible for drawing and positioning the shapes (2 bounded tetrominoes) on screen.
 */
public class ShapeDraw extends TetrominoDraw {

    public ShapeDraw(int[][] internalArray, Point startCoordinates, SizeObserver sizeObserver) {
        super(internalArray, startCoordinates, null, sizeObserver, null);
        setColor(0, Color.WHITE); //set white color for outer shape
    }

    public ShapeDraw(int[][] internalArray, SizeObserver sizeObserver) {
        this(internalArray, new Point(0, 0), sizeObserver);
    }

    /**
     * Starts the FadeOutAnimation of the shape.
     * @param shape A {@link Shape} object that should be notified, if the animation finished.
     */
    public void startFadeOutAnimation(Shape shape) {
        Thread alphaAnimation = new Thread(new Runnable() {
            @Override
            public void run() {
                while (alpha > 0) {
                    alpha -= 5;
                    shape.repaint();

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                //if fade out is complete, notify Shape
                shape.onFadeOutAnimationFinished();

            }
        });
        alphaAnimation.start();

    }

    public void resetAlpha() {
        alpha = 255;
    }

    /**
     * Sets tetromino's color.
     *
     * @param index The index of the added tetromino, starting with 1. Thus, the first added tetromino has the index 1.
     * @param color The color the tetromino at the specified index should have.
     */
    public void setColor(int index, Color color) {
        colorArray[index] = color;
    }

    // Need to override this method, as shape needs to draw a intersecting path that confuse the path creating.
    // This could happen if one tetromino is already fitted into the shape and cuts the path of the outer shape, so that two forms are created and thus, the path ignores some lines.
    @Override
    public GeneralPath getPathOutOfLines(ArrayList<Line> shapeLines) {
        GeneralPath path = new GeneralPath();

        for (Line line : shapeLines) {
            path.moveTo(line.x0, line.y0);
            path.lineTo(line.x1, line.y1);
        }

        return path;
    }

    @Override
    public Color getAnimationColor(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }


}
