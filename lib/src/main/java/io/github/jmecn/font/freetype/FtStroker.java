package io.github.jmecn.font.freetype;

import static org.lwjgl.util.freetype.FreeType.FT_Stroker_Done;
import static org.lwjgl.util.freetype.FreeType.FT_Stroker_Set;

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

    public void set(long radius, int lineCap, int lineJoin, long miterLimit) {
        FT_Stroker_Set(address, radius, lineCap, lineJoin, miterLimit);
    }

    public long address() {
        return address;
    }
}
