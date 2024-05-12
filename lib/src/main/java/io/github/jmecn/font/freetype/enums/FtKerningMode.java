package io.github.jmecn.font.freetype.enums;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public enum FtKerningMode {
    /** Return grid-fitted kerning distances in 26.6 fractional pixels. */
    DEFAULT,
    /* Return un-grid-fitted kerning distances in 26.6 fractional pixels. */
    UNFITTED,
    /* Return the kerning vector in original font units. */
    UNSCALED;


}
