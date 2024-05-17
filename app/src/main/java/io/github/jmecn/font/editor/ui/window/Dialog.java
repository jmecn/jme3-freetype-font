package io.github.jmecn.font.editor.ui.window;

import com.simsilica.lemur.Panel;
import lombok.NonNull;

public interface Dialog {

    /**
     * Sets the window manager.
     * Used internally.
     *
     * @param windowManager the WindowManager to set.
     */
    void setWindowManager(@NonNull WindowManager windowManager);
    @NonNull Panel getDialogPanel();

    /**
     * Closes the dialog.
     */
    void closeDialog();

    /**
     * An event that is fired when a button is pressed.
     * @param button the button that has been pressed.
     */
    void buttonPressed(DialogButton button);

}
