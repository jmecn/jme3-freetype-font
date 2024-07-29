package io.github.jmecn.font;

import com.jme3.math.Vector2f;
import io.github.jmecn.math.BaseTransform;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface FontStrike {
    FontFile getFontResource();
    float getSize();
    BaseTransform getTransform();
    boolean drawAsShapes();

    int getQuantizedPosition(Vector2f point);
    Metrics getMetrics();
    Glyph getGlyph(char symbol);
    Glyph getGlyph(int glyphCode);
    void clearDesc(); // for cache management.
    int getAAMode();

    /* These are all user space values */
    float getCharAdvance(char ch);
}
