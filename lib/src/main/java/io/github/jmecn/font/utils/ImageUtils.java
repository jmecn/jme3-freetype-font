package io.github.jmecn.font.utils;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import io.github.jmecn.font.freetype.FtBitmap;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.util.freetype.FreeType.*;

public final class ImageUtils {
    private ImageUtils() {}

    public static Image ftBitmapToImage(FtBitmap bitmap, ColorRGBA color, float gamma) {
        int width = bitmap.getWidth();
        int rows = bitmap.getRows();
        ByteBuffer src = bitmap.getBuffer();
        Image image;
        int pixelMode = bitmap.getPixelMode();
        int rowBytes = Math.abs(bitmap.getPitch()); // We currently ignore negative pitch.
        if (color == ColorRGBA.White && pixelMode == FT_PIXEL_MODE_GRAY && rowBytes == width && gamma == 1) {
            ByteBuffer data = BufferUtils.clone(src);
            image = new Image(Image.Format.Luminance8, width, rows, data, ColorSpace.Linear);
        } else {
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
        }

        return image;
    }

    public static void drawMono(Image image, FtBitmap bitmap, ColorRGBA color) {
        int width = bitmap.getWidth();
        int rows = bitmap.getRows();
        ByteBuffer src = bitmap.getBuffer();
        int rowBytes = Math.abs(bitmap.getPitch()); // We currently ignore negative pitch.

        ByteBuffer data = image.getData(0);

        byte[] srcRow = new byte[rowBytes];
        int[] dstRow = new int[width];
        IntBuffer dst = data.asIntBuffer();

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
    }
    public static void drawGray(Image image, FtBitmap bitmap, ColorRGBA color, float gamma) {
        int width = bitmap.getWidth();
        int rows = bitmap.getRows();
        ByteBuffer src = bitmap.getBuffer();
        int rowBytes = Math.abs(bitmap.getPitch()); // We currently ignore negative pitch.

        ByteBuffer data = image.getData(0);

        byte[] srcRow = new byte[rowBytes];
        int[] dstRow = new int[width];
        IntBuffer dst = data.asIntBuffer();

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
                    dstRow[x] = rgba;
                else
                    dstRow[x] = rgb | (int)(a * (float)Math.pow(alpha / 255f, gamma)); // Inverse gamma.
            }
            dst.put(dstRow);
        }
    }

    public static void drawBGRA(Image image, FtBitmap bitmap, ColorRGBA color, float gamma) {
        int width = bitmap.getWidth();
        int rows = bitmap.getRows();
        ByteBuffer src = bitmap.getBuffer();
        int rowBytes = Math.abs(bitmap.getPitch()); // We currently ignore negative pitch.

        ByteBuffer data = image.getData(0);

        byte[] srcRow = new byte[rowBytes];
        int[] dstRow = new int[width];
        IntBuffer dst = data.asIntBuffer();

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
                    red = (int) (red * color.r);
                }
                if (alpha != 255) {
                    alpha = (int)(0xFF * (float)Math.pow(alpha / 255f, gamma) * color.a); // Inverse gamma.
                    // alpha = (int)(alpha * color.a);
                }
                dstRow[i / 4] = (red << 24) | (green << 16) | (blue << 8) | alpha;
            }
            dst.put(dstRow);
        }
    }

    public static Image newImage(Image.Format format, int width, int height) {
        int capacity = format.getBitsPerPixel() * width * height / 8;
        return new Image(format, width, height, ByteBuffer.allocateDirect(capacity), ColorSpace.Linear);
    }

    public static void drawImage(Image dest, Image source, int x, int y) {
        ImageRaster writer = ImageRaster.create(dest);
        ImageRaster reader = ImageRaster.create(source);
        ColorRGBA src = new ColorRGBA();
        ColorRGBA dst = new ColorRGBA();

        int height = source.getHeight();
        int width = source.getWidth();
        for (int yPos = 0; yPos < height; yPos++) {
            for (int xPos = 0; xPos < width; xPos++) {
                // get
                reader.getPixel(xPos, yPos, src);
                writer.getPixel(xPos + x, yPos + y, dst);
                // set
                // blend mode: sumAlpha
                float srcAlpha = src.a;
                float dstAlpha = dst.a;
                dst.multLocal(1f - srcAlpha).addLocal(src.multLocal(srcAlpha));
                dst.a = srcAlpha + dstAlpha;// sum alpha
                writer.setPixel(xPos + x, yPos + y, dst);
            }
        }
        dest.setUpdateNeeded();
    }

    public static void drawImage(Image destination, Image source, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight) {
        ImageRaster writer = ImageRaster.create(destination);
        ImageRaster reader = ImageRaster.create(source);
        ColorRGBA src = new ColorRGBA();
        ColorRGBA dst = new ColorRGBA();
        for (int y = 0; y < srcWidth; y++) {
            for (int x = 0; x < srcHeight; x++) {
                // get
                reader.getPixel(srcX + x, srcY + y, src);
                writer.getPixel(dstX + x, dstY + y, dst);
                // set
                // blend mode: sumAlpha
                float srcAlpha = src.a;
                float dstAlpha = dst.a;
                dst.multLocal(1f - srcAlpha).addLocal(src.multLocal(srcAlpha));
                dst.a = srcAlpha + dstAlpha;
                writer.setPixel(dstX + x, dstY + y, dst);
            }
        }
    }
}
