package io.github.jmecn.font.editor.ui.window;

import com.jme3.input.MouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.event.MouseListener;

public class ButtonHighlighter implements MouseListener {

    private final IconComponent iconComponent;
    private final ColorRGBA color;

    private boolean isHovered;

    public ButtonHighlighter(IconComponent iconComponent) {
        this.iconComponent = iconComponent;
        this.color = iconComponent.getColor();
    }

    @Override public void mouseButtonEvent(MouseButtonEvent event, Spatial target, Spatial capture) {

        boolean isPressed = event.isPressed() && event.getButtonIndex() == MouseInput.BUTTON_LEFT;


        if (isPressed) {
            this.iconComponent.setColor(color.mult(.8f));
        }
        else {
            if (target != null ) {

                if (isHovered) {
                    this.iconComponent.setColor(color.mult(1.6f));
                }
                else {
                    this.iconComponent.setColor(color);
                }

            }

        }
    }

    @Override
    public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture) {
        isHovered = true;
        this.iconComponent.setColor(color.mult(1.2f));
    }

    @Override
    public void mouseExited(MouseMotionEvent event, Spatial target, Spatial capture) {
        isHovered = false;
        this.iconComponent.setColor(color);
    }

    @Override
    public void mouseMoved(MouseMotionEvent event, Spatial target, Spatial capture) {

    }


}