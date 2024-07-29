package io.github.jmecn.font;

import io.github.jmecn.text.RectBounds;

public interface Glyph {
    int getGlyphCode();
    /* These 3 are user space values */
    RectBounds getBBox();
    float getAdvance();
    float getPixelXAdvance();
    float getPixelYAdvance();

    /* These 4 methods should only be called after either getPixelData() or
     * getPixelData(int subPixel) is invoked. This ensures the returned value
     * is correct for the requested subpixel position. */
    int getWidth();
    int getHeight();
    int getOriginX();
    int getOriginY();
}