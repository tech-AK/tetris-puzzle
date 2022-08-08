package tetris.puzzles.datamodels;

/**
 * This class holds a data model for saving the start and end coordinates of a Line.
 */
public class Line implements Comparable<Line> {

    public int x0;
    public int y0;

    public int x1;
    public int y1;

    /**
     * Constructor of a new line
     * @param x0 The start coordinate on x axis of the new line
     * @param y0 The start coordinate on y axis of the new line
     * @param x1 The end coordinate on x axis of the new line
     * @param y1 The end coordinate on y axis of the new line
     */
    public Line(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }


    /**
     * Compares the given line object to this line. The comparison is done by the start coordinates of the line.
     * <ol>
     *     <li>Compare the x0 (start point on x-axis) against each other.</li>
     *     <li>If equal, compare the the y0 (start point on y-axis) against each other.</li>
     *     <li>If still equal, both lines are considered as equal and thus, 0 is returned.</li>
     * </ol>
     */
    @Override
    public int compareTo(Line o) {
        int diff = x0 - o.x0;
        if (diff == 0) {
            return y0 - o.y0;
        } else {
            return diff;
        }
    }
}
