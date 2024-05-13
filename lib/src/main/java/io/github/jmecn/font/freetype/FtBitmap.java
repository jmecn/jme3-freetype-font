package io.github.jmecn.font.freetype;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import io.github.jmecn.font.utils.ImageUtils;
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
        return bitmap.buffer(row * bitmap.pitch());
    }

    public long address() {
        return address;
    }

    public Image getPixmap (Image.Format format, ColorRGBA color, float gamma) {
        Image pixmap = ImageUtils.ftBitmapToImage(this, color, gamma);

        Image converted = pixmap;
        if (format != pixmap.getFormat()) {
            int capacity = pixmap.getWidth() *pixmap.getHeight() *format.getBitsPerPixel() / 8;
            ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

            converted = new Image(format, pixmap.getWidth(), pixmap.getHeight(), buffer, ColorSpace.Linear);
            // Draw
            ImageUtils.drawImage(converted, pixmap, 0, 0);
            pixmap.dispose();
        }
        return converted;
    }
}
