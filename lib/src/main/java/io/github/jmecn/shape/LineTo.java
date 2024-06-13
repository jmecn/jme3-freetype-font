package io.github.jmecn.shape;

import java.awt.geom.Path2D;
import java.nio.file.Path;

/**
 * Creates a line path element by drawing a straight line
 * from the current coordinate to the new coordinates.
 *
 * <p>For more information on path elements see the {@link Path} and
 * {@link PathElement} classes.
 *
 * <p>Example:
 *
<PRE>
import javafx.scene.shape.*;

Path path = new Path();
path.getElements().add(new MoveTo(0.0f, 50.0f));
path.getElements().add(new LineTo(100.0f, 100.0f));
</PRE>
 * @since JavaFX 2.0
 */
public class LineTo extends PathElement {

    /**
     * Creates a new isntance of LineTo.
     * @param x the horizontal coordinate of the line end point
     * @param y the vertical coordinate of the line end point
     */
    public LineTo(double x, double y) {
        setX(x);
        setY(y);
    }

    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns a string representation of this {@code LineTo} object.
     * @return a string representation of this {@code LineTo} object.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LineTo[");
        sb.append("x=").append(getX());
        sb.append(", y=").append(getY());
        return sb.append("]").toString();
    }
}