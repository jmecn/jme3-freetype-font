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
            ByteBuffer data = BufferUtils.clone(src);
            pixmap = new Image(Image.Format.Alpha8, width, rows, data, ColorSpace.Linear);

        } else {
            ByteBuffer data = BufferUtils.createByteBuffer(width * rows * 4);
            pixmap = new Image(Image.Format.RGBA8, width, rows, data, ColorSpace.Linear);
            int rgba = color.asIntRGBA();
            byte[] srcRow = new byte[rowBytes];
            int[] dstRow = new int[width];
            IntBuffer dst = data.asIntBuffer();
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
            int capacity = pixmap.getWidth() *pixmap.getHeight() *format.getBitsPerPixel() / 8;
            ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

            converted = new Image(format, pixmap.getWidth(), pixmap.getHeight(), buffer, ColorSpace.Linear);
            // Draw
            drawImage(converted, pixmap, 0, 0);
            pixmap.dispose();
        }
        return converted;
    }

    private void drawImage(Image dest, Image source, int x, int y) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int destSize = destWidth * destHeight * dest.getFormat().getBitsPerPixel() / 8;
        byte[] image = new byte[destSize];

        ByteBuffer sourceData = source.getData(0);
        int height = source.getHeight();
        int width = source.getWidth();
        for (int yPos = 0; yPos < height; yPos++) {
            for (int xPos = 0; xPos < width; xPos++) {
                int i = ((xPos + x) + (yPos + y) * destWidth) * 4;
                if (source.getFormat() == Image.Format.ABGR8) {
                    int j = (xPos + yPos * width) * 4;
                    image[i] = sourceData.get(j); //a
                    image[i + 1] = sourceData.get(j + 1); //b
                    image[i + 2] = sourceData.get(j + 2); //g
                    image[i + 3] = sourceData.get(j + 3); //r
                } else if (source.getFormat() == Image.Format.BGR8) {
                    int j = (xPos + yPos * width) * 3;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j + 2); //r
                } else if (source.getFormat() == Image.Format.RGB8) {
                    int j = (xPos + yPos * width) * 3;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j + 2); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.RGBA8) {
                    int j = (xPos + yPos * width) * 4;
                    image[i] = sourceData.get(j + 3); //a
                    image[i + 1] = sourceData.get(j + 2); //b
                    image[i + 2] = sourceData.get(j + 1); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.Luminance8) {
                    int j = (xPos + yPos * width) * 1;
                    image[i] = 1; //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j); //g
                    image[i + 3] = sourceData.get(j); //r
                } else if (source.getFormat() == Image.Format.Luminance8Alpha8) {
                    int j = (xPos + yPos * width) * 2;
                    image[i] = sourceData.get(j + 1); //a
                    image[i + 1] = sourceData.get(j); //b
                    image[i + 2] = sourceData.get(j); //g
                    image[i + 3] = sourceData.get(j); //r
                } else {
                    throw new UnsupportedOperationException("Cannot draw textures with format " + source.getFormat());
                }
            }
        }

        ByteBuffer destData = dest.getData(0);
        destData.put(image);
        destData.flip();
        dest.setUpdateNeeded();
    }
}
