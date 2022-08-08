package tetris.puzzles.interfaces;

/**
 * This interface specifies four methods. They can be used to specify the property of the components dynamically
 * using the current height and width of the panel. All components which are sensitive to panel's size change should
 * implement this interface and provide logic to handle the re-sizing of the components in dependence of the panel's total height and width.
 *
 * @deprecated This interface is no longer used. It's better to use the built-in layout operations swing provide to achieve the functionality of this interface.
 */
@Deprecated
public interface ResizableComponent {

    /**
     * The implementation of this method should handle how the x-position of a component should be
     * determined if the panel resizes.
     *
     * @param currentPanelWidth  This method gets the current panel's width which can (but not has to be) used in order to determine the x position.
     * @param currentPanelHeight This method gets the current panel's height which can (but not has to be) used in order to determine the component's height.
     * @return the x-position of the component
     */
    int calculateXPos(int currentPanelWidth, int currentPanelHeight);

    /**
     * The implementation of this method should handle how the y-position of a component should be
     * determined if the panel resizes.
     *
     * @param currentPanelWidth  This method gets the current panel's width which can (but not has to be) used in order to determine the x position.
     * @param currentPanelHeight This method gets the current panel's height which can (but not has to be) used in order to determine the y position.
     * @return the y-position of the component
     */
    int calculateYPos(int currentPanelWidth, int currentPanelHeight);

    /**
     * The implementation of this method should handle how the width of a component should be
     * determined if the panel resizes.
     *
     * @param currentPanelWidth  This method gets the current panel's width which can (but not has to be) used in order to determine the component's width.
     * @param currentPanelHeight This method gets the current panel's height which can (but not has to be) used in order to determine the component's height.
     * @return the width of the component
     */
    int calculateWidth(int currentPanelWidth, int currentPanelHeight);

    /**
     * The implementation of this method should handle how the height of a component should be
     * determined if the panel resizes.
     *
     * @param currentPanelWidth  This method gets the current panel's width which can (but not has to be) used in order to determine the x position.
     * @param currentPanelHeight This method gets the current panel's height which can (but not has to be) used in order to determine the component's height.
     * @return the height of the component
     */
    int calculateHeight(int currentPanelWidth, int currentPanelHeight);
}
