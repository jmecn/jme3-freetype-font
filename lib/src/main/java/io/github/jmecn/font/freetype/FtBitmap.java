package io.github.jmecn.font.freetype;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import org.lwjgl.util.freetype.FT_Bitmap;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.util.freetype.FreeType.FT_PIXEL_MODE_GRAY;
import static org.lwjgl.util.freetype.FreeType.FT_PIXEL_MODE_MONO;

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

    public Image getPixmap (Image.Format format, ColorRGBA color, float gamma) {
        int width = getWidth();
        int rows = getRows();
        ByteBuffer src = getBuffer();
        Image pixmap;
        int pixelMode = getPixelMode();
        int rowBytes = Math.abs(getPitch()); // We currently ignore negative pitch.
        if (color == ColorRGBA.White && pixelMode == FT_PIXEL_MODE_GRAY && rowBytes == width && gamma == 1) {
            pixmap = new Image(Image.Format.Alpha8, width, rows, null, ColorSpace.Linear);
            BufferUtils.copy(src, pixmap.getPixels(), pixmap.getPixels().capacity());
        } else {
            pixmap = new Image(Image.Format.RGBA8, width, rows, null, ColorSpace.Linear);
            int rgba = color.asIntRGBA();
            byte[] srcRow = new byte[rowBytes];
            int[] dstRow = new int[width];
            IntBuffer dst = pixmap.getPixels().asIntBuffer();
            if (pixelMode == FT_PIXEL_MODE_MONO) {
                // Use the specified color for each set bit.
                for (int y = 0; y < rows; y++) {
                    src.get(srcRow);
                    for (int i = 0, x = 0; x < width; i++, x += 8) {
                        byte b = srcRow[i];
                        for (int ii = 0, n = Math.min(8, width - x); ii < n; ii++) {
                            if ((b & (1 << (7 - ii))) != 0)
                                dstRow[x + ii] = rgba;
                            else
                                dstRow[x + ii] = 0;
                        }
                    }
                    dst.put(dstRow);
                }
            } else {
                // Use the specified color for RGB, blend the FreeType bitmap with alpha.
                int rgb = rgba & 0xffffff00;
                int a = rgba & 0xff;
                for (int y = 0; y < rows; y++) {
                    src.get(srcRow);
                    for (int x = 0; x < width; x++) {
                        // Zero raised to any power is always zero.
                        // 255 (=one) raised to any power is always one.
                        // We only need Math.pow() when alpha is NOT zero and NOT one.
                        int alpha = srcRow[x] & 0xff;
                        if (alpha == 0)
                            dstRow[x] = rgb;
                        else if (alpha == 255)
                            dstRow[x] = rgb | a;
                        else
                            dstRow[x] = rgb | (int)(a * (float)Math.pow(alpha / 255f, gamma)); // Inverse gamma.
                    }
                    dst.put(dstRow);
                }
            }
        }

        Image converted = pixmap;
        if (format != pixmap.getFormat()) {
            converted = new Image(format, pixmap.getWidth(), pixmap.getHeight());
            converted.setBlending(Blending.None);
            converted.drawPixmap(pixmap, 0, 0);
            converted.setBlending(Blending.SourceOver);
            pixmap.dispose();
        }
        return converted;
    }
}
