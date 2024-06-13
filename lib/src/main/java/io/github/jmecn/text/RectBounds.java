package io.github.jmecn.text;

import com.jme3.math.Vector2f;
import io.github.jmecn.font.packer.Rectangle;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class RectBounds extends BaseBounds {
    // minimum x value of bounding box
    private float minX;
    // maximum x value of bounding box
    private float maxX;
    // minimum y value of bounding box
    private float minY;
    // maximum y value of bounding box
    private float maxY;

    /**
     * Create an axis aligned bounding rectangle object, with an empty bounds
     * where maxX < minX and maxY < minY.
     */
    public RectBounds() {
        minX = minY = 0.0f;
        maxX = maxY = -1.0f;
    }

    @Override public BaseBounds copy() {
        return new RectBounds(minX, minY, maxX, maxY);
    }

    /**
     * Creates a RectBounds based on the minX, minY, maxX, and maxY values specified.
     */
    public RectBounds(float minX, float minY, float maxX, float maxY) {
        setBounds(minX, minY, maxX, maxY);
    }

    /**
     * Creates a RectBounds object as a copy of the specified RectBounds object.
     */
    public RectBounds(RectBounds other) {
        setBounds(other);
    }

    /**
     * Creates a RectBounds object as a copy of the specified RECTANGLE.
     */
    public RectBounds(Rectangle other) {
        setBounds(other.getX(), other.getY(),
                other.getX() + other.getWidth(), other.getY() + other.getHeight());
    }

    @Override public BoundsType getBoundsType() {
        return BoundsType.RECTANGLE;
    }

    @Override public boolean is2D() {
        return true;
    }

    /**
     * Convenience function for getting the width of this RectBounds.
     * The dimension along the X-Axis.
     */
    @Override public float getWidth() {
        return maxX - minX;
    }

    /**
     * Convenience function for getting the height of this RectBounds
     * The dimension along the Y-Axis.
     */
    @Override public float getHeight() {
        return maxY - minY;
    }

    /**
     * Convenience function for getting the depth of this RectBounds
     * The dimension along the Z-Axis, since this is a 2D bounds the return
     * value is always 0.0f.
     */
    @Override public float getDepth() {
        return 0.0f;
    }

    @Override public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    @Override public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    @Override public float getMinZ() {
        return 0.0f;
    }

    @Override public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    @Override public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    @Override public float getMaxZ() {
        return 0.0f;
    }

    @Override public Vector2f getMin(Vector2f min) {
        if (min == null) {
            min = new Vector2f();
        }
        min.x = minX;
        min.y = minY;
        return min;
    }

    @Override public Vector2f getMax(Vector2f max) {
        if (max == null) {
            max = new Vector2f();
        }
        max.x = maxX;
        max.y = maxY;
        return max;
    }

    @Override public BaseBounds deriveWithUnion(BaseBounds other) {
        if (other.getBoundsType() == BoundsType.RECTANGLE) {
            RectBounds rb = (RectBounds) other;
            unionWith(rb);
        } else {
            throw new UnsupportedOperationException("Unknown BoundsType");
        }
        return this;
    }

    @Override public BaseBounds deriveWithNewBounds(Rectangle other) {
        if (other.getWidth() < 0 || other.getHeight() < 0) return makeEmpty();
        setBounds(other.getX(), other.getY(),
                other.getX() + other.getWidth(),
                other.getY() + other.getHeight());
        return this;
    }

    @Override public BaseBounds deriveWithNewBounds(BaseBounds other) {
        if (other.isEmpty()) return makeEmpty();
        if (other.getBoundsType() == BoundsType.RECTANGLE) {
            RectBounds rb = (RectBounds) other;
            minX = rb.getMinX();
            minY = rb.getMinY();
            maxX = rb.getMaxX();
            maxY = rb.getMaxY();
        } else {
            throw new UnsupportedOperationException("Unknown BoundsType");
        }
        return this;
    }

    @Override public BaseBounds deriveWithNewBounds(float minX, float minY, float maxX, float maxY) {
        if ((maxX < minX) || (maxY < minY)) return makeEmpty();
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        return this;
    }

    /**
     * Set the bounds to match that of the RectBounds object specified. The
     * specified bounds object must not be null.
     */
    public final void setBounds(RectBounds other) {
        minX = other.getMinX();
        minY = other.getMinY();
        maxX = other.getMaxX();
        maxY = other.getMaxY();
    }

    /**
     * Set the bounds to the given values.
     */
    public final void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Sets the bounds based on the given coords, and also ensures that after
     * having done so that this RectBounds instance is normalized.
     */
    public void setBoundsAndSort(float minX, float minY, float maxX, float maxY) {
        setBounds(minX, minY, maxX, maxY);
        sortMinMax();
    }

    @Override public void setBoundsAndSort(float minX, float minY,  float minZ,
                                           float maxX, float maxY, float maxZ) {
        if (minZ != 0 || maxZ != 0) {
            throw new UnsupportedOperationException("Unknown BoundsType");
        }
        setBounds(minX, minY, maxX, maxY);
        sortMinMax();
    }

    @Override public void setBoundsAndSort(Vector2f p1, Vector2f p2) {
        setBoundsAndSort(p1.x, p1.y, p2.x, p2.y);
    }

    // Note: this implementation is exactly the same as BoxBounds. I could put a default
    // implementation in BaseBounds which calls the getters, or I could move the minX, minY
    // etc up to BaseBounds, or I could (maybe?) have BoxBounds extend from RectBounds or
    // have both extend a common parent. In the end I wanted direct access to the fields
    // but this was the only way to get it without making a more major change.
    @Override public RectBounds flattenInto(RectBounds bounds) {
        // Create the bounds if we need to
        if (bounds == null) bounds = new RectBounds();
        // Make it empty if we need to
        if (isEmpty()) return bounds.makeEmpty();
        // Populate it with values otherwise
        bounds.setBounds(minX, minY, maxX, maxY);
        return bounds;
    }

    public void unionWith(RectBounds other) {
        // Short circuit union if either bounds is empty.
        if (other.isEmpty()) return;
        if (this.isEmpty()) {
            setBounds(other);
            return;
        }

        minX = Math.min(minX, other.getMinX());
        minY = Math.min(minY, other.getMinY());
        maxX = Math.max(maxX, other.getMaxX());
        maxY = Math.max(maxY, other.getMaxY());
    }

    public void unionWith(float minX, float minY, float maxX, float maxY) {
        // Short circuit union if either bounds is empty.
        if ((maxX < minX) || (maxY < minY)) return;
        if (this.isEmpty()) {
            setBounds(minX, minY, maxX, maxY);
            return;
        }

        this.minX = Math.min(this.minX, minX);
        this.minY = Math.min(this.minY, minY);
        this.maxX = Math.max(this.maxX, maxX);
        this.maxY = Math.max(this.maxY, maxY);
    }

    @Override public void add(float x, float y, float z) {
        if (z != 0) {
            throw new UnsupportedOperationException("Unknown BoundsType");
        }
        unionWith(x, y, x, y);
    }

    public void add(float x, float y) {
        unionWith(x, y, x, y);
    }

    @Override public void add(Vector2f p) {
        add(p.x, p.y);
    }

    @Override public void intersectWith(BaseBounds other) {
        // Short circuit intersect if either bounds is empty.
        if (this.isEmpty()) return;
        if (other.isEmpty()) {
            makeEmpty();
            return;
        }

        minX = Math.max(minX, other.getMinX());
        minY = Math.max(minY, other.getMinY());
        maxX = Math.min(maxX, other.getMaxX());
        maxY = Math.min(maxY, other.getMaxY());
    }

    @Override public void intersectWith(Rectangle other) {
        float x = other.getX();
        float y = other.getY();
        intersectWith(x, y, x + other.getWidth(), y + other.getHeight());
    }

    public void intersectWith(float minX, float minY, float maxX, float maxY) {
        // Short circuit intersect if either bounds is empty.
        if (this.isEmpty()) return;
        if ((maxX < minX) || (maxY < minY)) {
            makeEmpty();
            return;
        }

        this.minX = Math.max(this.minX, minX);
        this.minY = Math.max(this.minY, minY);
        this.maxX = Math.min(this.maxX, maxX);
        this.maxY = Math.min(this.maxY, maxY);
    }

    @Override public void intersectWith(float minX, float minY, float minZ,
                                        float maxX, float maxY, float maxZ) {
        // Short circuit intersect if either bounds is empty.
        if (this.isEmpty()) return;
        if ((maxX < minX) || (maxY < minY) || (maxZ < minZ)) {
            makeEmpty();
            return;
        }

        this.minX = Math.max(this.minX, minX);
        this.minY = Math.max(this.minY, minY);
        this.maxX = Math.min(this.maxX, maxX);
        this.maxY = Math.min(this.maxY, maxY);
    }

    @Override public boolean contains(Vector2f p) {
        if ((p == null) || isEmpty()) return false;
        return (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY);
    }

    @Override public boolean contains(float x, float y) {
        if (isEmpty()) return false;
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    /**
     * Determines whether the given <code>other</code> RectBounds is completely
     * contained within this RectBounds. Equivalent RectBounds will return true.
     *
     * @param other The other rect bounds to check against.
     * @return Whether the other rect bounds is contained within this one, which also
     * includes equivalence.
     */
    public boolean contains(RectBounds other) {
        if (isEmpty() || other.isEmpty()) return false;
        return minX <= other.minX && maxX >= other.maxX && minY <= other.minY && maxY >= other.maxY;
    }

    @Override public boolean intersects(float x, float y, float width, float height) {
        if (isEmpty()) return false;
        return (x + width >= minX &&
                y + height >= minY &&
                x <= maxX &&
                y <= maxY);
    }

    public boolean intersects(BaseBounds other) {
        if ((other == null) || other.isEmpty() || isEmpty()) {
            return false;
        }
        return (other.getMaxX() >= minX &&
                other.getMaxY() >= minY &&
                other.getMaxZ() >= getMinZ() &&
                other.getMinX() <= maxX &&
                other.getMinY() <= maxY &&
                other.getMinZ() <= getMaxZ());
    }

    @Override public boolean disjoint(float x, float y, float width, float height) {
        if (isEmpty()) return true;
        return (x + width < minX ||
                y + height < minY ||
                x > maxX ||
                y > maxY);
    }

    public boolean disjoint(RectBounds other) {
        if ((other == null) || other.isEmpty() || isEmpty()) {
            return true;
        }
        return (other.getMaxX() < minX ||
                other.getMaxY() < minY ||
                other.getMinX() > maxX ||
                other.getMinY() > maxY);
    }

    @Override public boolean isEmpty() {
        // NaN values will cause the comparisons to fail and return "empty"
        return !(maxX >= minX && maxY >= minY);
    }

    /**
     * Adjusts the edges of this RectBounds "outward" toward integral boundaries,
     * such that the rounded bounding box will always full enclose the original
     * bounding box.
     */
    @Override public void roundOut() {
        minX = (float) Math.floor(minX);
        minY = (float) Math.floor(minY);
        maxX = (float) Math.ceil(maxX);
        maxY = (float) Math.ceil(maxY);
    }

    public void grow(float h, float v) {
        minX -= h;
        maxX += h;
        minY -= v;
        maxY += v;
    }

    // for convenience, this function returns a reference to itself, so we can
    // change from using "bounds.makeEmpty(); return bounds;" to just
    // "return bounds.makeEmpty()"
    @Override public RectBounds makeEmpty() {
        minX = minY = 0.0f;
        maxX = maxY = -1.0f;
        return this;
    }

    @Override protected void sortMinMax() {
        if (minX > maxX) {
            float tmp = maxX;
            maxX = minX;
            minX = tmp;
        }
        if (minY > maxY) {
            float tmp = maxY;
            maxY = minY;
            minY = tmp;
        }
    }

    @Override public void translate(float x, float y, float z) {
        setMinX(getMinX() + x);
        setMinY(getMinY() + y);
        setMaxX(getMaxX() + x);
        setMaxY(getMaxY() + y);
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final RectBounds other = (RectBounds) obj;
        if (minX != other.getMinX()) return false;
        if (minY != other.getMinY()) return false;
        if (maxX != other.getMaxX()) return false;
        if (maxY != other.getMaxY()) return false;
        return true;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Float.floatToIntBits(minX);
        hash = 79 * hash + Float.floatToIntBits(minY);
        hash = 79 * hash + Float.floatToIntBits(maxX);
        hash = 79 * hash + Float.floatToIntBits(maxY);
        return hash;
    }

    @Override public String toString() {
        return "RectBounds { minX:" + minX + ", minY:" + minY + ", maxX:" + maxX + ", maxY:" + maxY + "} (w:" + (maxX-minX) + ", h:" + (maxY-minY) +")";
    }
}
