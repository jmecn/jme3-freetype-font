package io.github.jmecn.font.editor.ui.menubar;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.ElementId;

import java.util.ArrayList;
import java.util.List;

public class LemurMenuBar {

    public enum Position {
        Left, Right
    }

    private final Container menuBarContainer;
    private final Container leftMenuBarContainer;
    private final Container rightMenuBarContainer;

    // the last column is "stretched", so we fill it with an empty label.
    // this will force all menu items to retain their intended size.
    private final StretchPanel leftSeparator;
    private final StretchPanel rightSeparator;

    private final List<MenuElement> menuItems = new ArrayList<>();

    private final int startZ;
    private final boolean horizontal;
    private final Camera cam;

    public LemurMenuBar(int startZOrder, Camera cam) {
        this(startZOrder, true, cam);
    }

    /**
     * Creates a new LemurMenuBar at the desired Z-Order.
     * All children items (menus, sub-menus, etc) will appear above this z-order.
     * Setting the start z-order too low may result in other GUI elements being able to occlude it!
     * @param startZOrder the starting z-order of the MenuBar.
     * @param horizontal determines whether this menu is horizontal (true) or vertical (false).
     */
    public LemurMenuBar(int startZOrder, boolean horizontal, Camera cam) {

        this.startZ = startZOrder;
        this.horizontal = horizontal;
        this.cam = cam;

        menuBarContainer = new Container(
                new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First),
                new ElementId("menu-bar"));

        leftMenuBarContainer = menuBarContainer.addChild(new Container(
                new SpringGridLayout(Axis.Y, Axis.X, FillMode.Last, FillMode.Last)), 0, 0);
        leftMenuBarContainer.setBackground(new QuadBackgroundComponent(ColorRGBA.BlackNoAlpha));

        rightMenuBarContainer = horizontal
                ? menuBarContainer.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First)), 0, 1)
                : menuBarContainer.addChild(new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.Last, FillMode.Last)), 0, 1);

        rightMenuBarContainer.setBackground(new QuadBackgroundComponent(ColorRGBA.BlackNoAlpha));

        menuBarContainer.setQueueBucket(RenderQueue.Bucket.Gui);

        menuBarContainer.setLocalTranslation(
                menuBarContainer.getLocalTranslation().x,
                menuBarContainer.getLocalTranslation().y,
                startZ);

        leftSeparator = new StretchPanel();
        rightSeparator = new StretchPanel();

    }

    public int getStartZ() {
        return startZ;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    Camera getCamera() {
        return cam;
    }

    /**
     * Sets the location of the menubar
     * The menu bar must be attached to the scene beforehand so it can check if it is a child of the GUI node.
     * This check ensures the menu and all children do not exceed the camera dimensions.
     * These checks are ignored if the menu is attached to the Root Node.
     *
     * @throws IllegalStateException if the menubar has not been added to the scene.
     *
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     */
    public void setLocation(float x, float y) {

        Vector3f prefSize = menuBarContainer.getPreferredSize();

        Node root = getRoot();

        if (root == null) {
            throw new IllegalStateException("The MenuBar must be attached to something before you can set its location.");
        }

        if (root.getName().equalsIgnoreCase("gui node")) {

            float endX = x + prefSize.x;
            float endY = y - prefSize.y;

            if (endX > cam.getWidth()) {
                x -= endX - cam.getWidth();
            }

            if (endY < 0) {
                y += endY * -1;
            }

        }

        menuBarContainer.setLocalTranslation(x, y, startZ);
    }

    Node getRoot() {

        Node root = menuBarContainer.getParent();

        while (root.getParent() != null) {
            root = root.getParent();
        }

        return root;
    }

    public Container getMenuBarContainer() {
        return menuBarContainer;
    }

    public Spatial getSpatial() {
        return menuBarContainer;
    }

    public <T extends MenuElement> T add(T element) {
        return add(element, Position.Left);
    }

    public <T extends MenuElement> T add(T element, Position position) {

        if (menuItems.contains(element)) {
            throw new IllegalArgumentException("You cannot add the same DevMenu item more than once.");
        }

        if (horizontal) {

            if (position == Position.Left) {
                leftMenuBarContainer.addChild(element.getPanel(), 0, leftMenuBarContainer.getChildren().size());
                menuItems.add(element);

                // add the separator last to stop the menuItems from stretching.
                leftMenuBarContainer.removeChild(leftSeparator);
                leftMenuBarContainer.addChild(leftSeparator, 0, leftMenuBarContainer.getChildren().size() + 1);
            }
            else if (position == Position.Right) {

                // add the separator first to stop the menuItems from stretching.
                rightMenuBarContainer.removeChild(rightSeparator);
                rightMenuBarContainer.addChild(rightSeparator, 0, 0);

                rightMenuBarContainer.addChild(element.getPanel(), 0, rightMenuBarContainer.getChildren().size() + 1);
                menuItems.add(element);
            }

        }
        else { // vertical

            if (position == Position.Left) {
                leftMenuBarContainer.addChild(element.getPanel(), leftMenuBarContainer.getChildren().size(), 0);
                menuItems.add(element);

                // add the separator last to stop the menuItems from stretching.
                leftMenuBarContainer.removeChild(leftSeparator);
                leftMenuBarContainer.addChild(leftSeparator, leftMenuBarContainer.getChildren().size() + 1, 0);

            }
            else if (position == Position.Right) {

                rightMenuBarContainer.addChild(element.getPanel(), rightMenuBarContainer.getChildren().size(), 0);
                menuItems.add(element);

                // add the separator last to stop the menuItems from stretching.
                rightMenuBarContainer.removeChild(rightSeparator);
                rightMenuBarContainer.addChild(rightSeparator, rightMenuBarContainer.getChildren().size() + 1, 0);
            }

        }

        element.setMenuBar(this);

        if (element instanceof Menu) {
            MouseEventControl.addListenersToSpatial(element.getPanel(), new ButtonMouseHandler((Menu)element));
        }

        return element;
    }

    public void remove(MenuElement element) {

        // we don't know which one it belongs to, but it can only belong to one of them.
        leftMenuBarContainer.removeChild(element.getPanel());
        rightMenuBarContainer.removeChild(element.getPanel());

        menuItems.remove(element);
    }

    public boolean remove(String text) {

        MenuElement element = menuItems.stream()
                .filter(menuItem -> menuItem.getText().equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);

        if (element != null) {
            remove(element);
        }

        return element != null;
    }

    public void hideAllMenus() {
        for (MenuElement element : menuItems) {
            if (element instanceof Menu) {
                recursivelyHide( (Menu) element );
            }

        }
    }

    public void hideMenu(Menu element) {
        recursivelyHide(element);
    }

    private void recursivelyHide(Menu jmeMenu) {

        jmeMenu.hideMenu();

        for (MenuElement element : jmeMenu.getChildren()) {
            if (element instanceof Menu) {
                recursivelyHide((Menu) element);
            }
        }
    }

    public MenuElement getMenu(String name) {
        return menuItems.stream()
                .filter(window -> window.getText().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void setPreferredWidth(float width) {
        // set this last so the height has been calculated properly because we add menu items.
        Vector3f prefSize = menuBarContainer.getPreferredSize();
        prefSize.setX(width);
        menuBarContainer.setPreferredSize(prefSize);
    }

    private class ButtonMouseHandler extends DefaultMouseListener {

        private final Menu source;

        public ButtonMouseHandler(Menu source) {
            this.source = source;
        }

        @Override
        public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture ) {

            // Buttons always consume their click events
            event.setConsumed();

            if (event.getButtonIndex() == 0 && event.isPressed()) {

                for (MenuElement menu : menuItems) {

                    if (!source.getText().equals(menu.getText())) {
                        if (menu instanceof Menu) {
                            hideMenu( (Menu) menu);
                        }
                    }

                }

            }

        }
    }

}