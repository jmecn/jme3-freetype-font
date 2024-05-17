package io.github.jmecn.font.editor.ui.menubar;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.ElementId;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Menu implements MenuElement {

    private final Button button;
    private final List<MenuElement> menuItems = new ArrayList<>();
    private final Container childrenContainer = new Container(new SpringGridLayout(), new ElementId("menu-children"));

    private boolean enabled = true;

    private LemurMenuBar menuBar;
    private MenuElement parent;

    public Menu(String text) {
        this(null, text);
    }

    public Menu(String icon, String text) {
        button = new Button(text, new ElementId("menu-item"));

        if (icon != null) {
            IconComponent iconComponent = new IconComponent(icon);
            button.setIcon(iconComponent);
        }

        button.addClickCommands(source -> toggleMenu());
    }

    @Override
    public LemurMenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    public void setMenuBar(LemurMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @Override
    public MenuElement getParent() {
        return parent;
    }

    @Override
    public void setParent(MenuElement parent) {
        this.parent = parent;
    }

    @Override
    public Panel getPanel() {
        return button;
    }

    @Override
    public String getText() {
        return button.getText();
    }

    @Override
    public void setText(@NonNull String text) {
        button.setText(text);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        button.setEnabled(enabled);

        if (!enabled) {
            button.setColor(ColorRGBA.Gray);
        }
        else {
            button.setColor(ColorRGBA.White);
        }
    }

    public <T extends MenuElement> T add(T element) {

        element.setParent(this);
        element.setMenuBar(menuBar);

        if (menuItems.contains(element)) {
            throw new IllegalArgumentException("You cannot add the same DevMenu item more than once.");
        }

        childrenContainer.addChild(element.getPanel());
        menuItems.add(element);

        return element;
    }

    public boolean remove(MenuElement element) {

        boolean exists = menuItems.remove(element);

        if (exists) {
            childrenContainer.removeChild(element.getPanel());
        }

        return exists;
    }

    public List<MenuElement> getChildren() {
        return menuItems;
    }

    public MenuElement getChild(String text) {
        return menuItems.stream()
                .filter(element -> element.getText().equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }

    public void hideMenu() {
        childrenContainer.removeFromParent();
    }

    private void toggleMenu() {

        if (childrenContainer.getParent() != null) {
            // childrenContainer.removeFromParent();
            menuBar.hideMenu(this);
        }
        else {

            // Get the root. According to SimpleApplication.class it's either going to be "Root Node" or "Gui Node".
            Node root = menuBar.getRoot();

            Vector3f location = new Vector3f();

            // if the parent is null, it's a root element.
            if (parent == null) {

                // if the menu-bar is horizontal display it directly below.
                // else display it next to the menu
                if (menuBar.isHorizontal()) {
                    location.x = button.getWorldTranslation().x;
                    location.y = button.getWorldTranslation().y - button.getPreferredSize().y;
                }
                else {
                    location.x = button.getWorldTranslation().x + button.getPreferredSize().x;
                    location.y = button.getWorldTranslation().y;
                }

                // we need to determine whether the child container is going to be outside the camera frustum.
                // if the menu bar isn't in the GUI viewport we should ignore this calculation.

                if (root.getName().equalsIgnoreCase("gui node")) {
                    Vector3f childContainerSize = childrenContainer.getPreferredSize();

                    int camWidth = menuBar.getCamera().getWidth();
                    int endPos = (int) Math.ceil(location.x + childContainerSize.x);

                    if (endPos > camWidth) {
                        location.x -= endPos - camWidth;
                    }
                }

                // set the z-index above the menu bar.
                int parentZ = menuBar.getStartZ();
                location.z = parentZ + ((Container)menuBar.getSpatial()).getPreferredSize().z + 1;

            }
            // if the parent is not null, it's a sub-menu, so display it "near" it.
            else {
                location.x = button.getWorldTranslation().x + button.getPreferredSize().x;
                location.y = button.getWorldTranslation().y;

                if (root.getName().equalsIgnoreCase("gui node")) {
                    Vector3f childContainerSize = childrenContainer.getPreferredSize();

                    int camWidth = menuBar.getCamera().getWidth();
                    int endPos = (int) Math.ceil(location.x + childContainerSize.x);

                    if (endPos > camWidth) {
                        location.x -= endPos - camWidth;
                    }
                }

                // set the z-index above its parent.
                int parentZ = (int) button.getWorldTranslation().z;
                location.z = parentZ + button.getPreferredSize().z + 1;
            }

            childrenContainer.setLocalTranslation(location);
            root.attachChild(childrenContainer);

        }
    }

}