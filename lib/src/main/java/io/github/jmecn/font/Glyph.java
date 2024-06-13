package io.github.jmecn.font;

import io.github.jmecn.text.RectBounds;

import java.awt.*;

public interface Glyph {
    public int getGlyphCode();
    /* These 3 are user space values */
    public RectBounds getBBox();
    public float getAdvance();
    public Shape getShape();
    /* The rest are in device space */
    public byte[] getPixelData();

    /**
     * Returns the glyph mask at the subpixel position specified by subPixel.
     *
     * @see FontStrike#getQuantizedPosition(com.jme3.math.Vector2f)
     */
    public byte[] getPixelData(int subPixel);
    public float getPixelXAdvance();
    public float getPixelYAdvance();
    public boolean isLCDGlyph();

    /* These 4 methods should only be called after either getPixelData() or
     * getPixelData(int subPixel) is invoked. This ensures the returned value
     * is correct for the requested subpixel position. */
    public int getWidth();
    public int getHeight();
    public int getOriginX();
    public int getOriginY();
}