package io.github.jmecn.font.utils;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.IntMap;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.Glyph;
import io.github.jmecn.font.freetype.FtBitmap;
import io.github.jmecn.font.freetype.FtGlyphMetrics;
import io.github.jmecn.font.freetype.FtLibrary;
import io.github.jmecn.font.freetype.enums.FtPixelMode;

import java.nio.ByteBuffer;

import static org.lwjgl.util.freetype.FreeType.FT_PIXEL_MODE_MONO;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DebugPrintUtils {

    public static void print(FtGlyphMetrics metrics) {
        long width = metrics.getWidth();
        long height = metrics.getHeight();
        long horiBearingX = metrics.getHoriBearingX();
        long horiBearingY = metrics.getHoriBearingY();
        long horiAdvance = metrics.getHoriAdvance();
        long vertBearingX = metrics.getVertBearingX();
        long vertBearingY = metrics.getVertBearingY();
        long vertAdvance = metrics.getVertAdvance();
        System.out.printf("FtGlyphMetrics[0x%X], width=%d, height=%d, horiBearingX=%d, horiBearingY=%d, horiAdvance=%d, vertBearingX=%d, vertBearingY=%d, vertAdvance=%d\n",
                metrics.getAddress(), FtLibrary.from26D6(width), FtLibrary.from26D6(height),
                FtLibrary.from26D6(horiBearingX), FtLibrary.from26D6(horiBearingY), FtLibrary.from26D6(horiAdvance),
                FtLibrary.from26D6(vertBearingX), FtLibrary.from26D6(vertBearingY), FtLibrary.from26D6(vertAdvance));
    }

    public static void printBitmapInfo(FtBitmap bitmap) {
        int pixelMode = bitmap.getPixelMode();
        FtPixelMode mode = FtPixelMode.getMode(pixelMode);
        System.out.printf("pixel_mode: %s, width:%d, height:%d, pitch:%d, buffer_size:%d\n", mode, bitmap.getWidth(), bitmap.getRows(), bitmap.getPitch(), bitmap.getBufferSize());
    }

    public static void print(FtBitmap bitmap) {
        int pixelMode = bitmap.getPixelMode();
        print(bitmap.getBuffer(), bitmap.getPitch(), bitmap.getRows(), pixelMode == FT_PIXEL_MODE_MONO);
    }

    public static void print(ByteBuffer buffer, int width, int height, boolean mono) {
        byte[] data = new byte[width * height];
        buffer.get(data);
        buffer.rewind();
        print(data, width, height, mono);
    }

    public static void print(byte[] data, int width, int height, boolean mono) {
        String chars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(24 + width * height * 3);
        for (int y = 0; y < height; y++) {
            for ( int x = 0; x < width; x++ ) {
                int index = y * width + x;
                byte b = data[index];
                if (mono) {
                    for (int i = 7; i >= 0; i--) {
                        int bit = (b>> i) & 0x1;

                        if (bit == 0) {
                            sb.append("  ");
                        } else {
                            sb.append("[]");
                        }
                    }
                    if (x == width - 1) {
                        sb.append('\n');
                    }
                } else {
                    if (b == 0) {
                        sb.append("  ");
                    } else {
                        int low = b & 0x0F;
                        int hi = (b & 0xF0) >> 4;
                        sb.append(chars.charAt(hi)).append(chars.charAt(low));
                    }
                    if (x < width - 1) {
                        sb.append(' ');
                    } else {
                        sb.append('\n');
                    }
                }
            }
        }
        System.out.println(sb);
    }

    public static void drawGlyphRect(FtBitmapCharacterSet data) {
        IntMap<ImageRaster> rasterMap = new IntMap<>();

        for (Glyph glyph : data.getGlyphs()) {
            ColorRGBA color = ColorRGBA.randomColor();

            ImageRaster raster = rasterMap.get(glyph.getPage());
            if (raster == null) {
                Material material = data.getMaterial(glyph.getPage());
                Texture texture = material.getTextureParam("ColorMap").getTextureValue();
                Image image = texture.getImage();
                raster = ImageRaster.create(image);
                rasterMap.put(glyph.getPage(), raster);
            }

            // draw rect but not fill it
            for (int y = 0; y < glyph.getHeight(); y++) {
                for (int x = 0; x < glyph.getWidth(); x++) {
                    if (x == 0 || x == glyph.getWidth() - 1 || y == 0 || y == glyph.getHeight() - 1) {
                        raster.setPixel(glyph.getX() + x, glyph.getY() + y, color);
                    }
                }
            }
        }
    }
}
