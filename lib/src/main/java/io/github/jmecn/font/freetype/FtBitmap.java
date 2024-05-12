package io.github.jmecn.font.freetype;

import com.jme3.util.BufferUtils;
import org.lwjgl.util.freetype.FT_Bitmap;

import java.nio.ByteBuffer;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtBitmap {
    private final long address;
    private final FT_Bitmap bitmap;
    public FtBitmap(FT_Bitmap bitmap) {
        this.bitmap = bitmap;
        this.address = bitmap.address();
    }

    public int getWidth() {
        return bitmap.width();
    }

    public int getRows() {
        return bitmap.rows();
    }

    public int getPitch() {
        return bitmap.pitch();
    }

    public int getNumGrays() {
        return bitmap.num_grays();
    }

    public int getPixelMode() {
        return bitmap.pixel_mode();
    }

    public long getPalette() {
        return bitmap.palette();
    }

    public int getPaletteMode() {
        return bitmap.palette_mode();
    }

    public int getBufferSize() {
        return bitmap.rows() * bitmap.pitch();
    }

    public ByteBuffer getBuffer() {
        int row = bitmap.rows();
        if (row == 0) {
            // empty glyph, such as whitespace ' '
            return BufferUtils.createByteBuffer(1);
        }
        return BufferUtils.createByteBuffer(row * bitmap.pitch());
    }

    public long address() {
        return address;
    }
}
