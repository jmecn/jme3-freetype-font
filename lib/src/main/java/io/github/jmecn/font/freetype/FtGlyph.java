package io.github.jmecn.font.freetype;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.jmecn.font.freetype.FtErrors.ok;
import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 * @see <a href="https://freetype.org/freetype2/docs/reference/ft2-glyph_management.html">glyph management</a>
 * @author yanmaoyuan
 */
public class FtGlyph implements AutoCloseable {
    static Logger logger = LoggerFactory.getLogger(FtGlyph.class);

    private FT_Glyph glyph;

    public FtGlyph(FT_Glyph glyph) {
        this.glyph = glyph;
    }

    public FtGlyph(long address) {
        this.glyph = FT_Glyph.create(address);
    }

    @Override
    public void close() {
        if (glyph != null) {
            FreeType.FT_Done_Glyph(glyph);
            glyph = null;
        }
    }

    /**
     * @return The format of the glyph's image.
     */
    public int getFormat() {
        return glyph.format();
    }

    /**
     * @return A 16.16 vector that gives the glyph's advance width.
     */
    public FT_Vector getAdvance() {
        return glyph.advance();
    }

    /**
     * Transform a glyph image if its format is scalable.
     * <p>Note: The 2x2 transformation matrix is also applied to the glyph's advance vector.</p>
     * @param matrix A pointer to a 2x2 matrix to apply.
     * @param delta A pointer to a 2d vector to apply. Coordinates are expressed in 1/64 of a pixel.
     */
    public void transform(FT_Matrix matrix, FT_Vector delta) {
        ok( FT_Glyph_Transform(glyph, matrix, delta) );
    }

    /**
     * The mode how the values of FT_Glyph_Get_CBox are returned.
     * @param bBoxMode
     * @return
     */
    public FT_BBox getBBox(int bBoxMode) {
        FT_BBox bBox = FT_BBox.create();
        FT_Glyph_Get_CBox(glyph, bBoxMode, bBox);
        return bBox;
    }

    public FtBitmapGlyph toBitmap(int renderMode) {
        return toBitmap(renderMode, null, false);
    }

    /**
     *
     * @param renderMode An enumeration that describes how the data is rendered.
     * @param origin A pointer to a vector used to translate the glyph image before rendering. Can be 0 (if no translation). The origin is expressed in 26.6 pixels.
     * @param destroy A boolean that indicates that the original glyph image should be destroyed by this function. It is never destroyed in case of error.
     */
    public FtBitmapGlyph toBitmap(int renderMode, FT_Vector origin, boolean destroy) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ptr.put(glyph.address());
            ptr.flip();
            ok ( FT_Glyph_To_Bitmap(ptr, renderMode, origin, destroy) );
            if (destroy) {
                glyph = null;
            }
            FT_BitmapGlyph bitmapGlyph = FT_BitmapGlyph.create(ptr.get(0));

            return new FtBitmapGlyph(bitmapGlyph);
        }
    }

    /**
     * Stroke a given outline glyph object with a given stroker.
     * @param stroker A stroker handle.
     * @param destroy If true, the source glyph object is destroyed on success.
     * @return new glyph
     */
    public FtGlyph stroke(FtStroker stroker, boolean destroy) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ptr.put(glyph.address());
            ptr.flip();

            ok(FT_Glyph_Stroke(ptr, stroker.address(), destroy));

            return new FtGlyph(ptr.get(0));
        }
    }

    public FtGlyph strokeBorder(FtStroker stroker, boolean inside, boolean destroy) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ptr.put(glyph.address());
            ptr.flip();

            ok(FT_Glyph_StrokeBorder(ptr, stroker.address(), inside, destroy));

            return new FtGlyph(ptr.get(0));
        }
    }
}
