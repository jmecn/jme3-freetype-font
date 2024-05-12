package io.github.jmecn.font.freetype;

import io.github.jmecn.font.exception.FtRuntimeException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.jmecn.font.freetype.FtErrors.ok;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtGlyphSlot {
    static Logger logger = LoggerFactory.getLogger(FtGlyphSlot.class);

    private final FT_GlyphSlot glyphSlot;
    private FtGlyphMetrics metrics;
    private FtBitmap bitmap;
    public FtGlyphSlot(FT_GlyphSlot glyphSlot) {
        this.glyphSlot = glyphSlot;
    }

    public int getGlyphIndex() {
        return glyphSlot.glyph_index();
    }

    public int getFormat() {
        return glyphSlot.format();
    }

    public FtGlyph getGlyph() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ptr = stack.mallocPointer(1);
            ok ( FreeType.FT_Get_Glyph(glyphSlot, ptr) );
            return new FtGlyph(ptr.get(0));
        } catch (FtRuntimeException e) {
            logger.error("Failed get glyph", e);
            return null;
        }
    }

    public FtGlyphMetrics getMetrics() {
        if (metrics == null) {
            metrics = new FtGlyphMetrics(glyphSlot.metrics());
        }
        return metrics;
    }

    public FtBitmap getBitmap() {
        if (bitmap == null) {
            bitmap = new FtBitmap(glyphSlot.bitmap());
        }
        return bitmap;
    }

}
