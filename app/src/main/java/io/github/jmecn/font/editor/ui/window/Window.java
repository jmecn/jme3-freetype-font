package io.github.jmecn.font.editor.ui.window;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Panel;
import lombok.NonNull;

public interface Window {

    /**
     * Attempts to close the window, requesting permission from the windowClosed() event.
     */
    void close();

    /**
     * Forcibly removes the window, bypassing the windowClosedEvent() response.
     */
    void remove();

    WindowManager getWindowManager();
    void setWindowManager(@NonNull WindowManager windowManager);

    /**
     * Returns the Container that makes up the window.
     * @return the Panel of the window.
     */
    @NonNull Panel getWindowPanel();

    /**
     * Gets the title of the window.
     *
     * @return the title of the window.
     */
    @NonNull String getTitle();

    /**
     * Sets the title of the window.
     *
     * @param title the title of the window to set.
     */
    void setTitle(String title);

    /**
     * Set the content of the window.
     * @param content the content of the window.
     */
    void setContent(Panel content);

    /**
     * Sets the location of the window.
     *
     * @param location the location of the window you wish to set.
     */
    void setLocation(Vector2f location);

    /**
     * Sets the location of the window.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    void setLocation(float x, float y);

    /**
     * Returns the location of the window.
     *
     * @return the location of the window.
     */
    @NonNull Vector2f getLocation();

    /**
     * Returns the calculated size of the window, or the size that has been set by the user.
     * @return the calculated size of the window, or the size that has been set by the user.
     */
    @NonNull Vector3f getPreferredSize();

    /**
     * Sets the preferred size of the window, overriding the calculated size.
     * @param preferredSize the preferred size of the window.
     */
    void setPreferredSize(Vector3f preferredSize);

    /**
     * Brings this window in front of all other windows.
     */
    void bringToFront();

    /**
     * Puts this window behind every window.
     */
    void sendToBack();

    /**
     * Centers the window to the middle of the application canvas.
     */
    void centerOnScreen();

    /**
     * Provides an update loop.
     * @param tpf time per frame.
     */
    void update(float tpf);

}
