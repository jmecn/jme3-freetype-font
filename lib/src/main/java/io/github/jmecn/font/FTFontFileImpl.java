package io.github.jmecn.font;

import io.github.jmecn.font.freetype.*;
import io.github.jmecn.math.BaseTransform;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Vector;

import static org.lwjgl.util.freetype.FreeType.*;

class FTFontFileImpl extends FontFileImpl {
    /*
     * Font files can be accessed by several threads. In general this are the
     * JFX thread (measuring) and the Prism thread (rendering). But, if a Text
     * node is no connected a Scene it can be used in any user thread, thus a
     * font resource can be accessed from any thread.
     *
     * Freetype resources are not thread safe. In this implementation each font
     * resources has its own FT_Face and FT_Library, and while it permits these
     * resources be used by different threads it does not allow concurrent access.
     * This is enforced by converging all operation (from FTFontStrike and
     * FTGlyph) to this object and synchronizing the access to the native
     * resources using the same lock.
     */
    private FtLibrary library;
    private FtFace face;
    private FTDisposer disposer;

    FTFontFileImpl(String name, String filename, int fIndex, boolean register,
                   boolean embedded, boolean copy, boolean tracked) throws Exception {
        super(name, filename, fIndex, register, embedded, copy, tracked);
        init();
    }

    private synchronized void init() {
        library = new FtLibrary();
        face = library.newFace(getFileName(), getFontIndex());
        if (!isRegistered()) {
            disposer = new FTDisposer(library, face);
            Disposer.addRecord(this, disposer);
        }
    }

    @Override
    protected PrismFontStrike<?> createStrike(float size, BaseTransform transform,
                                              int aaMode, FontStrikeDesc desc) {
        return new FTFontStrike(this, size, transform, aaMode, desc);
    }

    @Override
    protected synchronized int[] createGlyphBoundingBox(int gc) {
        face.loadGlyph(gc, FT_LOAD_NO_SCALE);
        int[] bbox = new int[4];
        FtGlyphSlot glyphRec = face.getGlyphSlot();
        if (glyphRec != null && glyphRec.getMetrics() != null) {
            FtGlyphMetrics gm = glyphRec.getMetrics();
            bbox[0] = (int)gm.getHoriBearingX();
            bbox[1] = (int)(gm.getHoriBearingY() - gm.getHeight());
            bbox[2] = (int)(gm.getHoriBearingX() + gm.getWidth());
            bbox[3] = (int)gm.getHoriBearingY();
        }
        return bbox;
    }

    synchronized void initGlyph(FTGlyph glyph, FTFontStrike strike) {
        float size = strike.getSize();
        if (size == 0) {
            return;
        }
        face.setCharSize(0, FtLibrary.float26D6(size), 72, 72);

        int flags = FT_LOAD_RENDER | FT_LOAD_NO_HINTING | FT_LOAD_NO_BITMAP;
        FT_Matrix matrix = strike.matrix;
        if (matrix != null) {
            FT_Vector delta = FT_Vector.create();
            delta.set(0, 0);
            face.setTransform(matrix, delta);
            delta.close();
        } else {
            flags |= FT_LOAD_IGNORE_TRANSFORM;
        }
        flags |= FT_FT_LOAD_TARGET_NORMAL;

        int glyphCode = glyph.getGlyphCode();
        if (!face.loadGlyph(glyphCode, flags)) {
            return;
        }

        FtGlyphSlot glyphRec = face.getGlyphSlot();
        if (glyphRec == null) {
            return;
        }
        FtBitmap bitmap = glyphRec.getBitmap();
        if (bitmap == null) {
            return;
        }

        FtGlyph ftGlyph = glyphRec.getGlyph();
        ftGlyph.getAdvance().x();
        ftGlyph.getAdvance().y();

        FtGlyphMetrics metrics = glyphRec.getMetrics();

        glyph.bitmap = bitmap;
//        glyph.bitmap_left = glyphRec.bitmap_left;
//        glyph.bitmap_top = glyphRec.bitmap_top;
//        glyph.advanceX = glyphRec.advance_x / 64f;    /* Fixed 26.6*/
//        glyph.advanceY = glyphRec.advance_y / 64f;
//        glyph.userAdvance = glyphRec.linearHoriAdvance / 65536.0f; /* Fixed 16.16 */
    }
}