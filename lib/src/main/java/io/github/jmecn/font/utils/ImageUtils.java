package io.github.jmecn.font.utils;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.texture.image.MipMapImageRaster;
import com.jme3.util.BufferUtils;
import io.github.jmecn.font.freetype.FtBitmap;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.util.freetype.FreeType.*;

/**
 * A singleton class for image utilities.
 * It holds a weak reference cache of ImageRaster for each Image.
 */
public final class ImageUtils {

    static final Map<Image, WeakReference<ImageRaster>> weakRefCache;

    static {
        weakRefCache = new ConcurrentHashMap<>();
    }

    public static Image ftBitmapToImage(FtBitmap bitmap, ColorRGBA color, float gamma) {
        int width = bitmap.getWidth();
        int rows = bitmap.getRows();
        ByteBuffer src = bitmap.getBuffer();
        Image image;
        int pixelMode = bitmap.getPixelMode();
        int rowBytes = Math.abs(bitmap.getPitch()); // We currently ignore negative pitch.

        image = newImage(Image.Format.RGBA8, width, rows);
        ByteBuffer data = image.getData(0);

        byte[] srcRow = new byte[rowBytes];
        int[] dstRow = new int[width];
        IntBuffer dst = data.asIntBuffer();
        if (pixelMode == FT_PIXEL_MODE_MONO) {
            // Use the specified color for each set bit.
            int rgba = color.asIntRGBA();
            for (int y = 0; y < rows; y++) {
                src.get(srcRow);
                for (int i = 0, x = 0; x < width; i++, x += 8) {
                    int b = srcRow[i] & 0xFF;
                    for (int ii = 0, n = Math.min(8, width - x); ii < n; ii++) {
                        if ((b & (1 << (7 - ii))) != 0)
                            dstRow[x + ii] = rgba;
                        else
                            dstRow[x + ii] = 0;
                    }
                }
                dst.put(dstRow);
            }
        } else if (pixelMode == FT_PIXEL_MODE_GRAY) {
            // Use the specified color for RGB, blend the FreeType bitmap with alpha.
            int rgba = color.asIntRGBA();
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
        } else if (pixelMode == FT_PIXEL_MODE_BGRA) {
            // ignore the input rgba, use the bgra
            // convert bgra to rgba
            for (int y = 0; y < rows; y++) {
                src.get(srcRow);
                for (int i = 0; i < srcRow.length; i += 4) {
                    int blue = srcRow[i] & 0xFF;
                    int green = srcRow[i + 1] & 0xFF;
                    int red = srcRow[i + 2] & 0xFF;
                    int alpha = srcRow[i + 3] & 0xFF;

                    // apply color and gamma correction
                    if (color.b >= 0f && color.b < 1f) {
                        // blue = (int) (0xFF * (float)Math.pow(blue / 255f, gamma) * color.b);
                        blue = (int) (blue * color.b);
                    }
                    if (color.g >= 0f && color.g < 1f) {
                        // green = (int) (0xFF * (float)Math.pow(green / 255f, gamma) * color.g);
                        green = (int) (green * color.g);
                    }
                    if (color.r >= 0f && color.r < 1f) {
                        // red = (int) (0xFF * (float)Math.pow(red / 255f, gamma) * color.r);
                        red = (int) (0xFF * red * color.r);
                    }
                    if (alpha != 255) {
                        alpha = (int)(0xFF * (float)Math.pow(alpha / 255f, gamma) * color.a); // Inverse gamma.
                    }
                    dstRow[i / 4] = (red << 24) | (green << 16) | (blue << 8) | alpha;
                }
                dst.put(dstRow);
            }
        }

        return image;
    }

    public static Image newImage(Image.Format format, int width, int height) {
        int capacity = format.getBitsPerPixel() * width * height / 8;
        return new Image(format, width, height, ByteBuffer.allocateDirect(capacity), ColorSpace.Linear);
    }

    /**
     * Xiaolin Wu's line algorithm
     *
     * @param destination
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param color
     */
    public static void drawLine(Image destination, int x0, int y0, int x1, int y1, ColorRGBA color) {
        ImageRaster writer = ImageRaster.create(destination);
        ColorRGBA c = new ColorRGBA(color);
        // draw anti-aliased line
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        if (steep) {
            int tmp;
            // swap x0, y0
            tmp = x0;
            x0 = y0;
            y0 = tmp;
            // swap x1, y1
            tmp = x1;
            x1 = y1;
            y1 = tmp;
        }

        if (x0 > x1) {
            int tmp;
            // swap x0, x1
            tmp = x0;
            x0 = x1;
            x1 = tmp;
            // swap y0, y1
            tmp = y0;
            y0 = y1;
            y1 = tmp;
        }

        double gradient;
        if (x0 == x1) {
            gradient = 1.0;
        } else {
            gradient = (double) (y1 - y0) / (x1 - x0);
        }

        // handle first endpoint
        double xend = x0;
        double yend = y0 + gradient * (xend - x0);
        double xgap = 0.5;
        int xpxl1 = x0;// this will be used in the main loop
        int xpxl2 = x1; //this will be used in the main loop
        int ypxl1 = (int) yend;

        double intery = yend + gradient;// first y-intersection for the main loop

        if (steep) {
            plot(writer, ypxl1, xpxl1, c, rfpart(yend) * xgap);
            plot(writer, ypxl1 + 1, xpxl1, c, fpart(yend) * xgap);
        } else {
            plot(writer, xpxl1, ypxl1, c, rfpart(yend) * xgap);
            plot(writer, xpxl1, ypxl1 + 1, c, fpart(yend) * xgap);
        }

        // handle second endpoint
        xend = x1;
        yend = y1 + gradient * (xend - x1);
        xgap = 0.5;
        int ypxl2 = (int) yend;
        if (steep) {
            plot(writer, ypxl2, xpxl2, c, rfpart(yend) * xgap);
            plot(writer, ypxl2 + 1, xpxl2, c, fpart(yend) * xgap);
        } else {
            plot(writer, xpxl2, ypxl2, c, rfpart(yend) * xgap);
            plot(writer, xpxl2, ypxl2 + 1, c, fpart(yend) * xgap);
        }

        // main loop
        if (steep) {
            for (int x = xpxl1 + 1; x <= xpxl2 - 1; x++) {
                plot(writer, (int) intery, x, c, rfpart(intery));
                plot(writer, (int) intery + 1, x, c, fpart(intery));
                intery += gradient;
            }
        } else {
            for (int x = xpxl1 + 1; x <= xpxl2 - 1; x++) {
                plot(writer, x, (int) intery, c, rfpart(intery));
                plot(writer, x, (int) intery + 1, c, fpart(intery));
                intery += gradient;
            }
        }
    }

    static double fpart(double x) {
        return x - (int) x;
    }
    static double rfpart(double x) {
        return (int) (x + 1.0) - x;
    }
    /*
    // integer part of x
function ipart(x) is
    return floor(x)

function round(x) is
    return ipart(x + 0.5)

// fractional part of x
function fpart(x) is
    return x - ipart(x)

function rfpart(x) is
    return 1 - fpart(x)
     */
    private static void plot(ImageRaster raster, int x, int y, ColorRGBA c, double brightness) {
        if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight()) {
            return;
        }
        // plot the pixel at (x, y) with brightness c (where 0 ≤ c ≤ 1)
        c.a = (float) brightness;
        raster.setPixel(x, y, c);
    }

    public static void drawRect(Image destination, int rectX, int rectY, int rectWidth, int rectHeight, ColorRGBA color, boolean flipY) {
        int x0 = rectX;
        int y0 = rectY;
        int x1 = rectX + rectWidth - 1;
        int y1 = rectY + rectHeight - 1;

        if (flipY) {
            y0 = destination.getHeight() - 1 - y0;
            y1 = destination.getHeight() - 1 - y1;
        }
        drawLine(destination, x0, y0, x1, y0, color);
        drawLine(destination, x0, y1, x1, y1, color);
        drawLine(destination, x0, y0, x0, y1, color);
        drawLine(destination, x1, y0, x1, y1, color);
    }

    public static void drawImage(Image destination, Image source, int x, int y) {
        drawImage(destination, source, 0, 0, source.getWidth(), source.getHeight(), x, y, false);
    }

    public static void drawImage(Image destination, Image source, int x, int y, boolean flipY) {
        drawImage(destination, source, 0, 0, source.getWidth(), source.getHeight(), x, y, flipY);
    }

    public static void drawImage(Image destination, Image source, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY) {
        drawImage(destination, source, srcX, srcY, srcWidth, srcHeight, dstX, dstY, false);
    }

    public static void drawImage(Image destination, Image source, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, boolean flipY) {
        ImageRaster writer;
        if (destination.hasMipmaps()) {
            writer = new MipMapImageRaster(destination, 0);
        } else {
            writer = ImageRaster.create(destination, 0);
        }
        ImageRaster reader = ImageRaster.create(source);

        int dstWidth = destination.getWidth();
        int dstHeight = destination.getHeight();

        ColorRGBA src = new ColorRGBA();
        ColorRGBA dst = new ColorRGBA();
        for (int y = 0; y < srcHeight; y++) {
            int sy = srcY + y;
            int dy;
            if (flipY) {
                dy = dstHeight - 1 - (dstY + y);
            } else {
                dy = dstY + y;
            }
            if (dy < 0 || dy >= dstHeight) {
                // out of bounds
                continue;
            }
            for (int x = 0; x < srcWidth; x++) {
                int sx = srcX + x;
                int dx = dstX + x;
                if (dx < 0 || dx >= dstWidth) {
                    // out of bounds
                    continue;
                }
                // get
                reader.getPixel(sx, sy, src);
                writer.getPixel(dx, dy, dst);
                // set
                // blend mode: sumAlpha
                float srcAlpha = src.a;
                float dstAlpha = dst.a;
                dst.multLocal(1f - srcAlpha).addLocal(src.multLocal(srcAlpha));
                dst.a = srcAlpha + dstAlpha;
                writer.setPixel(dx, dy, dst);
            }
        }
    }
}
