package io.github.jmecn.font.editor.ui.menubar;

import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.style.ElementId;
import lombok.NonNull;

public class MenuItem implements MenuElement {

    private final Button button;
    private boolean enabled = true;

    private LemurMenuBar menuBar;
    private MenuElement parent;

    public MenuItem(String text) {
        this(null, text);
    }

    public MenuItem(String icon, String text) {
        button = new Button(text, new ElementId("menu-item"));

        if (icon != null) {
            IconComponent iconComponent = new IconComponent(icon);
            button.setIcon(iconComponent);
        }

        //noinspection unchecked
        button.addClickCommands(source -> {

            MenuElement parent = getParent();

            // if there's no parent it means it's a top-level menu element item.
            if (parent != null) {
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }

                menuBar.hideMenu((Menu) parent);
            }

        });
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

    public void addClickCommand(Command<Button> command) {
        //noinspection unchecked
        button.addClickCommands(command);
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
        button.setEnabled(enabled);
        this.enabled = enabled;

        if (!enabled) {
            button.setColor(ColorRGBA.Gray);
        }
        else {
            button.setColor(ColorRGBA.White);
        }
    }

}