package io.github.jmecn.font.freetype.enums;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public enum FtRenderMode {

    /**
     * Default render mode; it corresponds to 8-bit anti-aliased bitmaps.
     */
    NORMAL,// = 0
    /**
     * This is equivalent to FT_RENDER_MODE_NORMAL.
     * It is only defined as a separate value because render modes are also used indirectly to define hinting algorithm selectors.
     * See FT_LOAD_TARGET_XXX for details.
     */
    LIGHT,
    /**
     * This mode corresponds to 1-bit bitmaps (with 2 levels of opacity).
     */
    MONO,
    /**
     * This mode corresponds to horizontal RGB and BGR subpixel displays like LCD screens. It produces 8-bit bitmaps that are 3 times the width of the original glyph outline in pixels, and which use the FT_PIXEL_MODE_LCD mode.
     */
    LCD,
    /**
     * This mode corresponds to vertical RGB and BGR subpixel displays (like PDA screens, rotated LCD displays, etc.). It produces 8-bit bitmaps that are 3 times the height of the original glyph outline in pixels and use the FT_PIXEL_MODE_LCD_V mode.
     */
    LCD_V,
    /**
     * This mode corresponds to 8-bit, single-channel signed distance field (SDF) bitmaps. Each pixel in the SDF grid is the value from the pixel's position to the nearest glyph's outline. The distances are calculated from the center of the pixel and are positive if they are filled by the outline (i.e., inside the outline) and negative otherwise. Check the note below on how to convert the output values to usable data.
     */
    SDF;
}