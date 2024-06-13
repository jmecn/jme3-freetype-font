package io.github.jmecn.text;

import com.jme3.math.Vector2f;
import io.github.jmecn.font.packer.Rectangle;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public abstract class BaseBounds {

    /**
     * The different types of BaseBounds that are currently supported.
     * We might support other types of bounds in the future (such as
     * SPHERE) which are also 2D or 3D but are defined in some way
     * other than with a bounding box. Such bounds can sometimes more
     * accurately represent the pixels
     */
    public static enum BoundsType {
        RECTANGLE, // A 2D axis-aligned bounding rectangle
        BOX,  // A 3D axis-aligned bounding box
    }

    // Only allow subclasses in this package
    BaseBounds() { }

    /**
     * Duplicates this instance. This differs from deriveWithNewBounds(other)
     * where "other" would be this, in that derive methods may return the
     * same instance, whereas copy will always return a new instance.
     */
    public abstract BaseBounds copy();

    /**
     * Return true if this bounds is of a 2D BoundsType, else false.
     */
    public abstract boolean is2D();

    public abstract BoundsType getBoundsType();

    /**
     * Convenience function for getting the width of this bounds.
     * The dimension along the X-Axis.
     */
    public abstract float getWidth();

    /**
     * Convenience function for getting the height of this bounds.
     * The dimension along the Y-Axis.
     */
    public abstract float getHeight();

    /**
     * Convenience function for getting the depth of this bounds.
     * The dimension along the Z-Axis.
     */
    public abstract float getDepth();

    public abstract float getMinX();

    public abstract float getMinY();

    public abstract float getMinZ();

    public abstract float getMaxX();

    public abstract float getMaxY();

    public abstract float getMaxZ();

    public abstract void translate(float x, float y, float z);

    public abstract Vector2f getMin(Vector2f min);

    public abstract Vector2f getMax(Vector2f max);

    public abstract BaseBounds deriveWithUnion(BaseBounds other);

    // TODO: Add variants of deriveWithNewBounds such as pair of Vec* (RT-26886)
    public abstract BaseBounds deriveWithNewBounds(Rectangle other);
    public abstract BaseBounds deriveWithNewBounds(BaseBounds other);

    public abstract void intersectWith(Rectangle other);
    public abstract void intersectWith(BaseBounds other);

    public abstract void intersectWith(float minX, float minY, float minZ,
                                       float maxX, float maxY, float maxZ);

    /**
     * Sets the bounds based on the given points, and also ensures that
     * after having done so that this bounds instance is sorted (x1<=x2 and y1<=y2).
     */
    public abstract void setBoundsAndSort(Vector2f p1, Vector2f p2);

    public abstract BaseBounds deriveWithNewBounds(float minX, float minY, float maxX, float maxY);

    public abstract void setBoundsAndSort(float minX, float minY, float minZ,
                                          float maxX, float maxY, float maxZ);

    // TODO: obsolete add and replace with deriveWithUnion(Vec2f v) and deriveWithUnion(Vec3f v)
    // (RT-26886)
    public abstract void add(Vector2f p);
    public abstract void add(float x, float y, float z);

    public abstract boolean contains(Vector2f p);

    public abstract boolean contains(float x, float y);

    public abstract boolean intersects(float x, float y, float width, float height);

    public abstract boolean isEmpty();

    public abstract void roundOut();

    /**
     * Sets the given RectBounds (or creates a new instance of bounds is null) to
     * have the minX, minY, maxX, and maxY of this BoxBounds, dropping the Z values.
     *
     * @param bounds The bounds to fill with values, or null. If null, a new RectBounds
     *               is returned. If not null, the given bounds will be populated and
     *               then returned
     * @return a non-null reference to a RectBounds containing the minX, minY, maxX, and
     * maxY of this BoxBounds.
     */
    public abstract RectBounds flattenInto(RectBounds bounds);

    public abstract BaseBounds makeEmpty();

    public abstract boolean disjoint(float x, float y, float width, float height);

    protected abstract void sortMinMax();

    public static BaseBounds getInstance(float minX, float minY,
                                         float maxX, float maxY) {
        return new RectBounds(minX, minY, maxX, maxY);
    }
}
