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
        return bitmap.rows() * Math.abs(bitmap.pitch());
    }

    public ByteBuffer getBuffer() {
        int row = bitmap.rows();
        if (row == 0) {
            // empty glyph, such as whitespace ' '
            return BufferUtils.createByteBuffer(1);
        }
        return bitmap.buffer(row * Math.abs(bitmap.pitch()));
    }

    public long address() {
        return address;
    }

    public Image getImage(Image.Format format, ColorRGBA color, float gamma) {
        Image image = ImageUtils.ftBitmapToImage(this, color, gamma);

        Image converted = image;
        if (format != image.getFormat()) {
            int capacity = image.getWidth() *image.getHeight() * format.getBitsPerPixel() / 8;
            ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

            converted = new Image(format, image.getWidth(), image.getHeight(), buffer, ColorSpace.Linear);
            // Draw
            ImageUtils.drawImage(converted, image, 0, 0);
            image.dispose();
        }
        return converted;
    }
}
