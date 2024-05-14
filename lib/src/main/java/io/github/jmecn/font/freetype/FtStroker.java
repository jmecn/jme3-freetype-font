package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_Outline;
import org.lwjgl.util.freetype.FT_Vector;

import static io.github.jmecn.font.freetype.FtErrors.ok;
import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtStroker implements AutoCloseable {
    private final long address;
    public FtStroker(long address) {
        this.address = address;
    }

    @Override
    public void close() {
        FT_Stroker_Done(address);
    }

    /**
     * Reset a stroker object's attributes.
     * @param radius The border radius.
     * @param lineCap The line cap style.
     * @param lineJoin The line join style.
     * @param miterLimit The maximum reciprocal sine of half-angle at the miter join, expressed as 16.16 fixed-point value.
     */
    public void set(long radius, int lineCap, int lineJoin, long miterLimit) {
        FT_Stroker_Set(address, radius, lineCap, lineJoin, miterLimit);
    }

    /**
     * Reset a stroker object without changing its attributes. You should call this function before beginning
     * a new series of calls to FT_Stroker_BeginSubPath or FT_Stroker_EndSubPath.
     */
    public void rewind() {
        FT_Stroker_Rewind(address);
    }

    /**
     * A convenience function used to parse a whole outline with the stroker. The resulting outline(s) can
     * be retrieved later by functions like FT_Stroker_GetCounts and FT_Stroker_Export.
     * @param outline The source outline.
     * @param opened A boolean. If true, the outline is treated as an open path instead of a closed one.
     */
    public void parseOutline(FT_Outline outline, boolean opened) {
        ok(FT_Stroker_ParseOutline(address, outline, opened));
    }

    public void beginSubPath(FT_Vector to, boolean open) {
        ok(FT_Stroker_BeginSubPath(address, to, open));
    }

    public void endSubPath() {
        ok(FT_Stroker_EndSubPath(address));
    }

    public void lineTo(FT_Vector to) {
        ok(FT_Stroker_LineTo(address, to));
    }

    public void conicTo(FT_Vector control, FT_Vector to) {
        ok(FT_Stroker_ConicTo(address, control, to));
    }

    public void cubicTo(FT_Vector control1, FT_Vector control2, FT_Vector to) {
        ok(FT_Stroker_CubicTo(address, control1, control2, to));
    }

    public long address() {
        return address;
    }
}
