package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_BitmapGlyph;

import static org.lwjgl.util.freetype.FreeType.nFT_Done_Glyph;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtBitmapGlyph extends FtGlyph implements AutoCloseable {
    private final FT_BitmapGlyph glyph;
    private final FtBitmap bitmap;

    public FtBitmapGlyph(FT_BitmapGlyph glyph) {
        super(glyph.root());
        this.glyph = glyph;
        this.bitmap = new FtBitmap(glyph.bitmap());
    }

    @Override
    public void close() {
        nFT_Done_Glyph(glyph.address());
    }

    public int getTop() {
        return glyph.top();
    }

    public int getLeft() {
        return glyph.left();
    }

    public FtBitmap getBitmap() {
        return bitmap;
    }
}