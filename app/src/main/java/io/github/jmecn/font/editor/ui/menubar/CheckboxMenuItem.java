package io.github.jmecn.font.editor.ui.menubar;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.core.CommandMap;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.ElementId;
import lombok.NonNull;

public class CheckboxMenuItem implements MenuElement {

    public static final String ELEMENT_ID = "menu-checkbox";

    private final Checkbox checkbox;
    private boolean enabled = true;

    private LemurMenuBar menuBar;
    private MenuElement parent;

    private final CommandMap<Checkbox, ButtonAction> commandMap;

    public CheckboxMenuItem(String text) {
        this(null, text);
    }

    public CheckboxMenuItem(String icon, String text) {
        checkbox = new Checkbox(text, new ElementId(ELEMENT_ID), null);

        if (icon != null) {
            IconComponent iconComponent = new IconComponent(icon);
            checkbox.setIcon(iconComponent);
        }

        MouseEventControl.addListenersToSpatial(checkbox, new ButtonMouseHandler());

        commandMap = new CommandMap<>(checkbox);

        commandMap.addCommands(ButtonAction.Click, source -> source.setChecked(!source.isChecked()));
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

    public void addClickCommand(Command<Checkbox> command) {
        commandMap.addCommands(ButtonAction.Click, command);
    }

    @Override
    public Panel getPanel() {
        return checkbox;
    }

    @Override
    public String getText() {
        return checkbox.getText();
    }

    @Override
    public void setText(@NonNull String text) {
        checkbox.setText(text);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        checkbox.setEnabled(enabled);

        if (!enabled) {
            checkbox.setColor(ColorRGBA.Gray);
        }
        else {
            checkbox.setColor(ColorRGBA.White);
        }
    }

    private class ButtonMouseHandler extends DefaultMouseListener {

        @Override
        public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture ) {

            event.setConsumed();

            if (isEnabled() && event.getButtonIndex() == 0 && event.isPressed()) {
                commandMap.runCommands(ButtonAction.Click);

                // traverse all the way up to the parent and hide all of the menus that are visible.
                MenuElement parent = getParent();

                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }

                menuBar.hideMenu((Menu) parent);

            }

        }

    }

}