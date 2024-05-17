package io.github.jmecn.font.editor.ui.menubar;

import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * A transparent panel used to stretch over the remaining space of a stretched menu to avoid the menu elements sharing
 * it equally.
 */
public class StretchPanel extends Panel {

    public StretchPanel() {
        super();
        setBackground(new QuadBackgroundComponent(ColorRGBA.BlackNoAlpha));
    }
}