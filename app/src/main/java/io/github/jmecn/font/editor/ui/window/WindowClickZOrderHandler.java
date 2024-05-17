package io.github.jmecn.font.editor.ui.window;

import com.jme3.input.MouseInput;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.DefaultCursorListener;

/**
 * Brings a window to the front it it was clicked.
 */
public class WindowClickZOrderHandler extends DefaultCursorListener {

    private final WindowManager windowManager;

    public WindowClickZOrderHandler(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public void cursorButtonEvent(CursorButtonEvent event, Spatial target, Spatial capture ) {

        if (event.getButtonIndex() != MouseInput.BUTTON_LEFT) {
            return;
        }

        if( event.isPressed() ) {

            // if a window is clicked, bring it to the front.

            String id;

            id = capture.getUserData(JmeWindow.WINDOW_ID);

            if (id == null) {
                id = capture.getParent().getUserData(JmeWindow.WINDOW_ID);
            }

            Window window = windowManager.getById(id);
            window.bringToFront();

        }

    }

}
