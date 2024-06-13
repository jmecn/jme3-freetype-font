package io.github.jmecn.text;

import com.jme3.math.Vector2f;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface GlyphList {

    /**
     * Returns the number of glyphs in GlyphList.
     */
    int getGlyphCount();

    /**
     * Returns the glyph code for the given glyphIndex.
     */
    int getGlyphCode(int glyphIndex);

    /**
     * The x position for the given glyphIndex relative the GlyphList.
     */
    float getPosX(int glyphIndex);

    /**
     * The y position for the given glyphIndex relative the GlyphList.
     */
    float getPosY(int glyphIndex);

    /**
     * Returns the width of the GlyphList
     */
    float getWidth();

    /**
     * Returns the height of the GlyphList
     */
    float getHeight();

    /**
     * See TextLine#getBounds()
     * (used outside text layout in rendering and span bounds)
     */
    RectBounds getLineBounds();

    /**
     * The top-left location of the GlyphList relative to
     * the origin of the Text Layout.
     */
    Vector2f getLocation();

    /**
     * Maps the given glyph index to the char offset.
     * (used during rendering (selection))
     */
    int getCharOffset(int glyphIndex);

    /**
     * Means that this GlyphList was shaped using complex processing (ICU),
     * either because it is complex script or because font features were
     * requested.
     * (used outside text layout in rendering)
     */
    boolean isComplex();

    /**
     * Used during layout children (for rich text)
     * can be null (for non-rich text) but never null for rich text.
     */
    TextSpan getTextSpan();
}
