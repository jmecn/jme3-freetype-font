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
    GRAY(2),
    GRAY2(3),
    GRAY4(4),
    LCD(5),
    LCD_V(6),
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