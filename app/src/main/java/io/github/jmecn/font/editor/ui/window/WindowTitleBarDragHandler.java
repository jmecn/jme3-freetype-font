package io.github.jmecn.font.editor.ui.window;

import com.jme3.input.MouseInput;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.event.DefaultCursorListener;

import java.util.function.Function;

/**
 * A drag handler for a window title bar.
 */
public class WindowTitleBarDragHandler extends DefaultCursorListener {

    private Vector2f drag = null;
    private Vector3f basePosition;
    private boolean consumeDrags = false;
    private boolean consumeDrops = false;

    private Function<Spatial, Spatial> draggableLocator = Function.identity();

    private final boolean clampToCameraLimits;

    public WindowTitleBarDragHandler(boolean clampToCameraLimits) {
        this.clampToCameraLimits = clampToCameraLimits;
    }

    /**
     *  Sets the function that will be used to find the draggable spatial
     *  relative to the spatial that was clicked.  By default, this is the identity()
     *  function and will return the spatial that was clicked.
     */
    public void setDraggableLocator(Function<Spatial, Spatial> draggableLocator ) {
        this.draggableLocator = draggableLocator;
    }

    public Function<Spatial, Spatial> getDraggableLocator() {
        return draggableLocator;
    }

    public boolean isDragging() {
        return drag != null;
    }

    protected Vector2f getDragStartLocation() {
        return drag;
    }

    /**
     *  Finds the draggable spatial for the specified capture spatial.
     *  By default this just returns the capture  because the parentLocator
     *  function is the identity function.  This can be overridden by specifying
     *  a different function or overriding this method.
     */
    protected Spatial findDraggable( Spatial capture ) {
        return draggableLocator.apply(capture);
    }

    protected void startDrag(CursorButtonEvent event, Spatial target, Spatial capture ) {
        drag = new Vector2f(event.getX(), event.getY());
        basePosition = findDraggable(capture).getWorldTranslation().clone();
        event.setConsumed();
    }

    protected void endDrag( CursorButtonEvent event, Spatial target, Spatial capture ) {
        if (consumeDrops) {
            event.setConsumed();
        }
        drag = null;
        basePosition = null;
    }

    @Override
    public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
        if (event.getButtonIndex() != MouseInput.BUTTON_LEFT) {
            return;
        }

        if( event.isPressed() ) {
            startDrag(event, target, capture.getParent());
        } else {
            // Dragging is done.
            // Only delegate the up events if we were dragging in the first place.
            if( drag != null ) {
                endDrag(event, target, capture);
            }
        }
    }

    @Override
    public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture ) {
        if (drag == null || capture == null) {
            return;
        }

        ViewPort vp = event.getViewPort();
        Camera cam = vp.getCamera();

        if( consumeDrags ) {
            event.setConsumed();
        }

        // If it's an ortho camera then we'll assume 1:1 mapping
        // for now.
        if( cam.isParallelProjection() || capture.getQueueBucket() == RenderQueue.Bucket.Gui ) {
            Vector2f current = new Vector2f(event.getX(), event.getY());
            Vector2f delta = current.subtract(drag);

            Container draggable = (Container) findDraggable(capture.getParent());

            // Make sure if Z has changed that it is applied to base
            basePosition.z = draggable.getWorldTranslation().z;

            // Convert the world position into local space
            Vector3f localPos = basePosition.add(delta.x, delta.y, 0);
            if( draggable.getParent() != null ) {
                localPos = draggable.getParent().worldToLocal(localPos, null);
            }

            // don't let the window get dragged out of view.
            if (clampToCameraLimits) {

                float clampedX = FastMath.clamp(localPos.x, 0, cam.getWidth() - draggable.getPreferredSize().x);
                float clampedY = FastMath.clamp(localPos.y, draggable.getPreferredSize().y, cam.getHeight());

                localPos.setX(clampedX);
                localPos.setY(clampedY);

            }

            draggable.setLocalTranslation(localPos);
            return;
        }

        // Figure out how far away the center of the spatial is
        Vector3f pos = basePosition;

        // Figure out what one "unit" up and down would be
        // at this distance.
        Vector3f v1 = cam.getScreenCoordinates(pos, null);
        Vector3f right = cam.getScreenCoordinates(pos.add(cam.getLeft().negate()), null);
        Vector3f up = cam.getScreenCoordinates(pos.add(cam.getUp()), null);

        Vector2f units = new Vector2f(right.x - v1.x, up.y - v1.y);

        // So... convert the actual screen movement to world space
        // movement along the camera plane.
        Vector2f current = new Vector2f(event.getX(), event.getY());
        Vector2f delta = current.subtract(drag);

        // Need to maintain the sign of the drag delta
        delta.x /= Math.abs(units.x);
        delta.y /= Math.abs(units.y);

        // Adjust the spatial's position accordingly
        Vector3f newPos = pos.add(cam.getLeft().mult(-delta.x));
        newPos.addLocal(cam.getUp().mult(delta.y));

        Spatial draggable = findDraggable(capture);
        Vector3f local = draggable.getParent().worldToLocal(newPos, null);
        draggable.setLocalTranslation(local);
    }

    public void setConsumeDrags( boolean consumeDrags ) {
        this.consumeDrags = consumeDrags;
    }

    public boolean getConsumeDrags() {
        return consumeDrags;
    }

    public void setConsumeDrops( boolean consumeDrops ) {
        this.consumeDrops = consumeDrops;
    }

    public boolean getConsumeDrops() {
        return consumeDrops;
    }
}
