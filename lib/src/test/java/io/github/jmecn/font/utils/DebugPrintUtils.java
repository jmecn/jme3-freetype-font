package io.github.jmecn.font.utils;

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
                metrics.getAddress(), FtLibrary.toInt(width), FtLibrary.toInt(height),
                FtLibrary.toInt(horiBearingX), FtLibrary.toInt(horiBearingY), FtLibrary.toInt(horiAdvance),
                FtLibrary.toInt(vertBearingX), FtLibrary.toInt(vertBearingY), FtLibrary.toInt(vertAdvance));
    }

    public static void print(FtBitmap bitmap) {
        int pixelMode = bitmap.getPixelMode();
        FtPixelMode mode = FtPixelMode.getMode(pixelMode);
        System.out.printf("PixelMode: %s\n", mode);
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
        sb.append("width=").append(width).append(", height=").append(height);
        System.out.println(sb);
    }
}
