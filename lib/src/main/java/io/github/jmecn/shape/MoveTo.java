package io.github.jmecn.shape;

import java.nio.file.Path;

/**
 * Creates an addition to the path by moving to the specified
 * coordinates.
 *
 * <p>For more information on path elements see the {@link Path} and
 * {@link PathElement} classes.
 *
 * <p>Example:
 *
<PRE>
import javafx.scene.shape.*;

Path path = new Path();
path.getElements().add(new MoveTo(0.0f, 0.0f));
path.getElements().add(new LineTo(100.0f, 100.0f));
</PRE>
 * @since JavaFX 2.0
 */
public class MoveTo extends PathElement {

    /**
     * Creates a new instance of MoveTo.
     * @param x the horizontal coordinate to move to
     * @param y the vertical coordinate to move to
     */
    public MoveTo(double x, double y) {
        setX(x);
        setY(y);
    }

    /**
     * Defines the specified X coordinate.
     *
     * @defaultValue 0.0
     */
    private double x;

    public final void setX(double value) {
        this.x = value;
    }

    public final double getX() {
        return x;
    }

    /**
     * Defines the specified Y coordinate.
     *
     * @defaultValue 0.0
     */
    private double y;

    public final void setY(double value) {
        this.y = value;
    }

    public final double getY() {
        return y;
    }

    /**
     * Returns a string representation of this {@code MoveTo} object.
     * @return a string representation of this {@code MoveTo} object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoveTo[");
        sb.append("x=").append(getX());
        sb.append(", y=").append(getY());
        return sb.append("]").toString();
    }
}