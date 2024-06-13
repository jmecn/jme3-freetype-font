package io.github.jmecn.font;

import com.jme3.math.Vector2f;
import io.github.jmecn.math.BaseTransform;
import io.github.jmecn.text.GlyphList;

import java.awt.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface FontStrike {
    public FontResource getFontResource();
    public float getSize();
    public BaseTransform getTransform();
    public boolean drawAsShapes();

    /**
     * Modifies the point argument to the quantized position suitable for the
     * underlying glyph rasterizer.
     * The return value is the sub pixel index which should be passed to
     * {@link Glyph#getPixelData(int)} in order to obtain the correct glyph mask
     * for the given point.
     */
    public int getQuantizedPosition(Vector2f point);
    public Metrics getMetrics();
    public Glyph getGlyph(char symbol);
    public Glyph getGlyph(int glyphCode);
    public void clearDesc(); // for cache management.
    public int getAAMode();

    /* These are all user space values */
    public float getCharAdvance(char ch);
    public Shape getOutline(GlyphList gl,
                            BaseTransform transform);
}
