package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;
import org.lwjgl.util.freetype.FT_Matrix;

class FTFontStrike extends PrismFontStrike<FTFontFileImpl> {
    FT_Matrix matrix;

    protected FTFontStrike(FTFontFileImpl fontResource, float size, BaseTransform tx, int aaMode, FontStrikeDesc desc) {
        super(fontResource, size, tx, aaMode, desc);
        if (!tx.isTranslateOrIdentity()) {
            BaseTransform tx2d = getTransform();
            matrix = FT_Matrix.create();
            /* Fixed 16.16 to int */
            matrix.xx((int)( tx2d.getMxx() * 65536.0f));
            matrix.yx((int)(-tx2d.getMyx() * 65536.0f)); /*Inverted coordinates system */
            matrix.xy((int)(-tx2d.getMxy() * 65536.0f)); /*Inverted coordinates system */
            matrix.yy((int)( tx2d.getMyy() * 65536.0f));
        }
    }

    @Override
    protected DisposerRecord createDisposer(FontStrikeDesc desc) {
        return null;
    }

    @Override
    protected Glyph createGlyph(int glyphCode) {
        return new FTGlyph(this, glyphCode);
    }

    void initGlyph(FTGlyph glyph) {
        FTFontFileImpl fontResource = getFontResource();
        fontResource.initGlyph(glyph, this);
    }

}