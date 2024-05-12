package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_Size_Metrics;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtSizeMetrics {
    private final FT_Size_Metrics metrics;

    public FtSizeMetrics(FT_Size_Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * The width of the scaled EM square in pixels, hence the term ‘ppem’ (pixels per EM). It is also referred to as ‘nominal width’.
     *
     * @return horizontal pixels per EM
     */
    public short getXPpem() {
        return metrics.x_ppem();
    }

    /**
     * The height of the scaled EM square in pixels, hence the term ‘ppem’ (pixels per EM). It is also referred to as ‘nominal height’.
     *
     * @return vertical pixels per EM
     */
    public short getYPpem() {
        return metrics.y_ppem();
    }

    /**
     * A 16.16 fractional scaling value to convert horizontal metrics from font units to 26.6 fractional pixels. Only relevant for scalable font formats.
     *
     * @return scaling values used to convert font
     */
    public long getXScale() {
        return metrics.x_scale();
    }

    /**
     * A 16.16 fractional scaling value to convert vertical metrics from font units to 26.6 fractional pixels. Only relevant for scalable font formats.
     *
     * @return units to 26.6 fractional pixels
     */
    public long getYScale() {
        return metrics.y_scale();
    }

    /**
     * The ascender in 26.6 fractional pixels, rounded up to an integer value. See FT_FaceRec for the details.
     *
     * @return ascender in 26.6 fractional pixels
     */
    public long getAscender() {
        return metrics.ascender();
    }

    /**
     * The descender in 26.6 fractional pixels, rounded down to an integer value. See FT_FaceRec for the details.
     *
     * @return descender in 26.6 fractional pixels
     */
    public long getDescender() {
        return metrics.descender();
    }

    /**
     * The height in 26.6 fractional pixels, rounded to an integer value. See FT_FaceRec for the details.
     *
     * @return text height in 26.6 fractional pixels
     */
    public long getHeight() {
        return metrics.height();
    }

    /**
     * The maximum advance width in 26.6 fractional pixels, rounded to an integer value. See FT_FaceRec for the details.
     *
     * @return max horizontal advance, in 26.6 pixels
     */
    public long getMaxAdvance() {
        return metrics.max_advance();
    }
}
