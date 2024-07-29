package io.github.jmecn.font;

import io.github.jmecn.font.freetype.FtBitmap;
import io.github.jmecn.text.RectBounds;

class FTGlyph implements Glyph {
    FTFontStrike strike;
    int glyphCode;
    FtBitmap bitmap;
    int bitmap_left;
    int bitmap_top;
    float advanceX;
    float advanceY;
    float userAdvance;

    FTGlyph(FTFontStrike strike, int glyphCode) {
        this.strike = strike;
        this.glyphCode = glyphCode;
    }

    @Override
    public int getGlyphCode() {
        return glyphCode;
    }

    private void init() {
        if (bitmap != null) {
            return;
        }
        strike.initGlyph(this);
    }

    @Override
    public RectBounds getBBox() {
        float[] bb = new float[4];
        FTFontFileImpl fontResource = strike.getFontResource();
        fontResource.getGlyphBoundingBox(glyphCode, strike.getSize(), bb);
        return new RectBounds(bb[0], bb[1], bb[2], bb[3]);
    }

    @Override
    public float getAdvance() {
        init();
        return userAdvance;
    }

    @Override
    public float getPixelXAdvance() {
        init();
        return advanceX;
    }

    @Override
    public float getPixelYAdvance() {
        init();
        return advanceY;
    }

    @Override
    public int getWidth() {
        init();
        /* Note: In Freetype the width is byte based */
        return bitmap != null ? bitmap.getWidth() : 0;
    }

    @Override
    public int getHeight() {
        init();
        return bitmap != null ? bitmap.getRows() : 0;
    }

    @Override
    public int getOriginX() {
        init();
        return bitmap_left;
    }

    @Override
    public int getOriginY() {
        init();
        return -bitmap_top; /* Inverted coordinates system */
    }

}