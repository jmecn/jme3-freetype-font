package io.github.jmecn.font.freetype.enums;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public enum FtPixelMode {
    /***
     * public static final int FT_PIXEL_MODE_NONE = 0;
     *     public static final int FT_PIXEL_MODE_MONO = 1;
     *     public static final int FT_PIXEL_MODE_GRAY = 2;
     *     public static final int FT_PIXEL_MODE_GRAY2 = 3;
     *     public static final int FT_PIXEL_MODE_GRAY4 = 4;
     *     public static final int FT_PIXEL_MODE_LCD = 5;
     *     public static final int FT_PIXEL_MODE_LCD_V = 6;
     *     public static final int FT_PIXEL_MODE_BGRA = 7;
     *     public static final int FT_PIXEL_MODE_MAX = 8;
     */
    NONE(0),// = 0
    /* This mode corresponds to 1-bit bitmaps (with 2 levels of opacity). */
    MONO(1),
    /* An 8-bit bitmap, generally used to represent anti-aliased glyph images. Each pixel is stored in one byte. Note that the number of ‘gray’ levels is stored in the num_grays field of the FT_Bitmap structure (it generally is 256). */
    GRAY(2),
    /* A 2-bit per pixel bitmap, used to represent embedded anti-aliased bitmaps in font files according to the OpenType specification. We haven't found a single font using this format, however. */
    GRAY2(3),
    /* A 4-bit per pixel bitmap, representing embedded anti-aliased bitmaps in font files according to the OpenType specification. We haven't found a single font using this format, however. */
    GRAY4(4),
    /* An 8-bit bitmap, representing RGB or BGR decimated glyph images used for display on LCD displays; the bitmap is three times wider than the original glyph image. See also FT_RENDER_MODE_LCD. */
    LCD(5),
    /* An 8-bit bitmap, representing RGB or BGR decimated glyph images used for display on rotated LCD displays; the bitmap is three times taller than the original glyph image. See also FT_RENDER_MODE_LCD_V. */
    LCD_V(6),
    /* [Since 2.5] An image with four 8-bit channels per pixel, representing a color image (such as emoticons) with alpha channel. For each pixel, the format is BGRA, which means, the blue channel comes first in memory. The color channels are pre-multiplied and in the sRGB colorspace. For example, full red at half-translucent opacity will be represented as ‘00,00,80,80’, not ‘00,00,FF,80’. See also FT_LOAD_COLOR. */
    BGRA(7),
    MAX(8);
    final int mode;

    FtPixelMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static FtPixelMode getMode(int value) {
        if (value < 0 || value >= MAX.mode) {
            throw new IllegalArgumentException("Unknown pixel mode:" + value);
        }
        for (FtPixelMode e : values()) {
            if (e.mode == value) {
                return e;
            }
        }
        return null;
    }
}