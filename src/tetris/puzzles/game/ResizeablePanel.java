package tetris.puzzles.game;

import tetris.puzzles.interfaces.ResizableComponent;

import javax.swing.*;
import java.util.ArrayList;

/**
 * This class extends {@link JPanel} and provides extra functionality to handle resizing of a panel
 * and all its internal components correctly without using a layout manager (useful if using complex custom views layout manger cannot handle).
 *
 * @deprecated The concept of a ResizablePanel is not maintained anymore. Use swing's built-in layout manager.
 */
@Deprecated
public class ResizeablePanel extends JPanel {

    private ArrayList<JComponent> list;

    ResizeablePanel() {
        setLayout(null); //disable layout manager
        list = new ArrayList<>(5);
    }

    /**
     * Adds a specific component to the panel and registers it as size-sensitive component.
     * If panel is notified via {@link #invalidateAllBounds()} that its size has changed,
     * all size-sensitive components added via this method will be resized according to the specified calculation steps
     * provided via the implementation of the {@link ResizableComponent} interface.
     * This method automatically takes care about repainting and showing component immediately after it was added.
     *
     * @param component The component which should be resized
     *                  <br><b>NOTE: The component HAS TO implement the {@link ResizableComponent} interface,
     *                  in order to specify how the component should be resized.</b>
     * @throws ClassCastException if component doesn't implement the {@link ResizableComponent} interface
     */
    void addResizingComponent(JComponent component) {
        if (component instanceof ResizableComponent) {
            list.add(component);
            add(component);
            invalidateBound(component); //need to set bounds to every new added component in order to show it on screen.
        } else {
            throw new ClassCastException("Component has to implement the ResizableComponent interface");
        }
    }


    void addResizingComponents(JComponent... components) {
        for (JComponent component : components) {
            addResizingComponent(component);
        }
    }

    /**
     * Tells that the size of the panel has changed and thus, that
     * the bounds of the added components are invalidated.
     * This method automatically resets the bounds of each component which was added previously via
     * {@link #addResizingComponent(JComponent) addResizingComponent(JComponent)}
     * with respect to the current size of the panel.
     */
    void invalidateAllBounds() {
        //need to set for every component bounds as using null layout
        if (list != null && !list.isEmpty()) { //only invalidateBounds if list not empty
            for (JComponent component : list) {
                invalidateBound(component);
            }
        }

    }

    void invalidateBound(JComponent component) {
        //need to set for every component bounds as using null layout

        ResizableComponent resizableComponent = (ResizableComponent) component;

        // using the resizableComponent in order to get the right values (x, y, width, height) in dependence
        // of the current panel width and height got via getWidth() and getHeight()
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int x = resizableComponent.calculateXPos(panelWidth, panelHeight);
        int y = resizableComponent.calculateYPos(panelWidth, panelHeight);
        int width = resizableComponent.calculateWidth(panelWidth, panelHeight);
        int height = resizableComponent.calculateHeight(panelWidth, panelHeight);

        component.setBounds(x, y, width, height);
    }
}
