package io.github.jmecn.font.freetype.enums;

import static org.lwjgl.util.freetype.FreeType.FT_PIXEL_MODE_MONO;
import static org.lwjgl.util.freetype.FreeType.FT_PIXEL_MODE_NONE;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public enum FtPixelMode {
    NONE,// = 0
    /* This mode corresponds to 1-bit bitmaps (with 2 levels of opacity). */
    MONO;
}