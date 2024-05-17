package io.github.jmecn.font.editor.ui.window;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.ElementId;
import io.github.jmecn.font.editor.exception.WindowNotInitializedException;
import lombok.NonNull;

import java.util.UUID;

public class JmeWindow implements Window {

    public static final String WINDOW_ID = "Window_ID";

    public static final String ELEMENT_ID_TITLE_BAR = "window-title-bar";
    public static final String ELEMENT_ID_TITLE_LABEL = "window-title-label";

    public static final String ELEMENT_ID_WINDOW_CONTENT_OUTER = "window-content-outer";
    public static final String ELEMENT_ID_WINDOW_CONTENT_INNER = "window-content-inner";

    private static final String ELEMENT_ID_BUTTON_MINIMIZE = "window-button-minimize";
    private static final String ELEMENT_ID_BUTTON_MAXIMIZE = "window-button-maximize";
    private static final String ELEMENT_ID_BUTTON_CLOSE = "window-button-close";

    private final Container windowContainer;
    private final Container titleContainer;
    private final Container contentParent;
    private final Container contentContainer;

    private final Label titleLabel;
    private final Button minButton;
    private final Button maxButton;
    private final Button closeButton;

    private CursorListener dragHandler;
    private CursorListener clickHandler;

    private WindowManager windowManager;

    private final ButtonHighlighter minButtonHighlighter;
    private final ButtonHighlighter maxButtonHighlighter;
    private final ButtonHighlighter closeButtonHighlighter;

    public JmeWindow() {
        this(null, null);
    }

    public JmeWindow(String title) {
        this(title, null);
    }

    public JmeWindow(String title, Panel content) {

        windowContainer = new Container("null");
        windowContainer.setUserData(WINDOW_ID, UUID.randomUUID().toString());

        // top bar (title, min, max, close)
        titleContainer = windowContainer.addChild(
                new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First),
                        new ElementId(ELEMENT_ID_TITLE_BAR)));

        // set the title if one has been given.
        if (title == null) {
            title = "";
        }
        titleLabel = titleContainer.addChild(new Label(title, new ElementId(ELEMENT_ID_TITLE_LABEL)), 0, 0);

        minButton = titleContainer.addChild(new Button("", new ElementId(ELEMENT_ID_BUTTON_MINIMIZE)), 0, 1);
        maxButton = titleContainer.addChild(new Button("", new ElementId(ELEMENT_ID_BUTTON_MAXIMIZE)), 0, 2);
        closeButton = titleContainer.addChild(new Button("", new ElementId(ELEMENT_ID_BUTTON_CLOSE)), 0, 3);

        contentParent = windowContainer.addChild(new Container(
                new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First),
                new ElementId(ELEMENT_ID_WINDOW_CONTENT_OUTER)));

        contentContainer = contentParent.addChild(new Container(
                new SpringGridLayout(),
                new ElementId(ELEMENT_ID_WINDOW_CONTENT_INNER)
        ));

        if (content != null) {
            contentContainer.addChild(content);
        }

        // button actions
        minButton.addCommands(Button.ButtonAction.Click, source -> contentParent.removeFromParent());
        maxButton.addCommands(Button.ButtonAction.Click, source -> windowContainer.addChild(contentParent));

        closeButton.addCommands(Button.ButtonAction.Click, source -> close());

        this.minButtonHighlighter = new ButtonHighlighter((IconComponent) minButton.getIcon());
        this.maxButtonHighlighter = new ButtonHighlighter((IconComponent) maxButton.getIcon());
        this.closeButtonHighlighter = new ButtonHighlighter((IconComponent) closeButton.getIcon());
    }

    /**
     * Attempts to close the window, requesting permission from the windowClosed() event.
     */
    @Override
    public void close() {

        if (windowClosing()) {

            if (windowManager == null) {
                throw new WindowNotInitializedException("You must add the window to the WindowManager!");
            }

            removeCursorEvents();
            windowManager.remove(this);
        }

    }

    /**
     * Forcibly removes the window, bypassing the windowClosedEvent() response.
     */
    @Override
    public void remove() {

        if (windowManager == null) {
            throw new WindowNotInitializedException("You must add the window to the WindowManager!");
        }


        removeCursorEvents();
        windowManager.remove(this);

    }

    private void addCursorEvents() {
        CursorEventControl.addListenersToSpatial(titleContainer, dragHandler);
        CursorEventControl.addListenersToSpatial(windowContainer, clickHandler);
        CursorEventControl.addListenersToSpatial(titleContainer, clickHandler);

        MouseEventControl.addListenersToSpatial(minButton, minButtonHighlighter);
        MouseEventControl.addListenersToSpatial(maxButton, maxButtonHighlighter);
        MouseEventControl.addListenersToSpatial(closeButton, closeButtonHighlighter);
    }

    protected void removeCursorEvents() {
        CursorEventControl.removeListenersFromSpatial(titleContainer, dragHandler);
        CursorEventControl.removeListenersFromSpatial(windowContainer, clickHandler);
        CursorEventControl.removeListenersFromSpatial(titleContainer, clickHandler);

        MouseEventControl.removeListenersFromSpatial(minButton, minButtonHighlighter);
        MouseEventControl.removeListenersFromSpatial(maxButton, maxButtonHighlighter);
        MouseEventControl.removeListenersFromSpatial(closeButton, closeButtonHighlighter);
    }

    @Override
    public @NonNull Panel getWindowPanel() {
        return windowContainer;
    }

    @Override
    public void setWindowManager(@NonNull WindowManager windowManager) {
        this.windowManager = windowManager;

        this.dragHandler = new WindowTitleBarDragHandler(true);
        this.clickHandler = new WindowClickZOrderHandler(windowManager);

        addCursorEvents();

        // center it by default. Let the user set another location if they prefer.
        // This way they won't get confused when a window doesn't appear.
        centerOnScreen();
    }

    @Override
    public WindowManager getWindowManager() {

        if (windowManager == null) {
            throw new WindowNotInitializedException("You must add the window to the WindowManager!");
        }

        return windowManager;
    }

    /**
     * Gets the title of the window.
     * @return the title of the window.
     */
    @Override
    public @NonNull String getTitle() {
        return titleLabel.getText();
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    @Override
    public void setContent(Panel content) {
        contentContainer.clearChildren();
        contentContainer.addChild(content);
    }

    /**
     * Sets the location of the window.
     *
     * @param location the location of the window you wish to set.
     */
    @Override
    public void setLocation(Vector2f location) {
        setLocation(location.x, location.y);
    }

    /**
     * Sets the location of the window.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    @Override
    public void setLocation(float x, float y) {
        windowContainer.setLocalTranslation(x, y, windowContainer.getLocalTranslation().z);
    }

    /**
     * Returns the location of the window.
     *
     * @return the location of the window.
     */
    @Override
    public @NonNull Vector2f getLocation() {
        return new Vector2f(windowContainer.getLocalTranslation().x, windowContainer.getLocalTranslation().y);
    }

    /**
     * Returns the calculated size of the window, or the size that has been set by the user.
     * @return the calculated size of the window, or the size that has been set by the user.
     */
    @Override
    public @NonNull Vector3f getPreferredSize() {
        return windowContainer.getPreferredSize();
    }

    /**
     * Sets the preferred size of the window, overriding the calculated size.
     * @param preferredSize the preferred size of the window.
     */
    @Override
    public void setPreferredSize(Vector3f preferredSize) {
        windowContainer.setPreferredSize(preferredSize);
    }

    /**
     * Brings this window in front of all other windows.
     */
    @Override
    public void bringToFront() {

        if (windowManager == null) {
            throw new WindowNotInitializedException("You must add the window to the WindowManager!");
        }

        windowManager.bringToFront(this);
    }

    /**
     * Puts this window behind every window.
     */
    @Override
    public void sendToBack() {

        if (windowManager == null) {
            throw new WindowNotInitializedException("You must add the window to the WindowManager!");
        }

        windowManager.sendToBack(this);
    }

    /**
     * This event is fired when a window is attempting to close, and can be cancelled by returning false.
     *
     * @return whether this close event is allowed to occur.
     */
    public boolean windowClosing() {

        return true;
    }

    @Override
    public void centerOnScreen() {

        if (windowManager == null) {
            throw new WindowNotInitializedException("You must add the window to the WindowManager!");
        }

        setLocation(
                windowManager.getApplication().getCamera().getWidth() * 0.5f - getPreferredSize().x * 0.5f,
                windowManager.getApplication().getCamera().getHeight() * 0.5f + getPreferredSize().y * 0.5f
        );
    }

    /**
     * Provides an update loop.
     * @param tpf time per frame.
     */
    @Override
    public void update(float tpf) {

    }

}
