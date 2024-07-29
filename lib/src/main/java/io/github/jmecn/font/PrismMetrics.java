package io.github.jmecn.font;

public class PrismMetrics implements Metrics {

    FontFileImpl fontResource;
    float ascent, descent, linegap;
    private float[] styleMetrics;
    float size;

    static final int XHEIGHT = 0;
    static final int CAPHEIGHT = 1;
    static final int TYPO_ASCENT = 2;
    static final int TYPO_DESCENT = 3;
    static final int TYPO_LINEGAP = 4;
    static final int STRIKETHROUGH_THICKNESS = 5;
    static final int STRIKETHROUGH_OFFSET = 6;
    static final int UNDERLINE_THICKESS = 7;
    static final int UNDERLINE_OFFSET = 8;
    static final int METRICS_TOTAL = 9;

    PrismMetrics(float ascent, float descent, float linegap,
                 FontFileImpl fontResource, float size) {
        this.ascent = ascent;
        this.descent = descent;
        this.linegap = linegap;
        this.fontResource = fontResource;
        this.size = size;
    }

    @Override
    public float getAscent() {
        return ascent;
    }

    @Override
    public float getDescent() {
        return descent;
    }

    @Override
    public float getLineGap() {
        return linegap;
    }

    @Override
    public float getLineHeight() {
        return -ascent + descent + linegap;
    }

    private void checkStyleMetrics() {
        if (styleMetrics == null) {
            styleMetrics = fontResource.getStyleMetrics(size);
        }
    }

    @Override
    public float getTypoAscent() {
        checkStyleMetrics();
        return styleMetrics[TYPO_ASCENT];
    }

    @Override
    public float getTypoDescent() {
        checkStyleMetrics();
        return styleMetrics[TYPO_DESCENT];
    }

    @Override
    public float getTypoLineGap() {
        checkStyleMetrics();
        return styleMetrics[TYPO_LINEGAP];
    }

    @Override
    public float getCapHeight() {
        checkStyleMetrics();
        return styleMetrics[CAPHEIGHT];
    }

    @Override
    public float getXHeight() {
        checkStyleMetrics();
        return styleMetrics[XHEIGHT];
    }

    @Override
    public float getStrikethroughOffset() {
        checkStyleMetrics();
        return styleMetrics[STRIKETHROUGH_OFFSET];
    }

    @Override
    public float getStrikethroughThickness() {
        checkStyleMetrics();
        return styleMetrics[STRIKETHROUGH_THICKNESS];
    }

    @Override
    public float getUnderLineOffset() {
        checkStyleMetrics();
        return styleMetrics[UNDERLINE_OFFSET];
    }

    @Override
    public float getUnderLineThickness() {
        checkStyleMetrics();
        return styleMetrics[UNDERLINE_THICKESS];
    }

    @Override
    public String toString() {
        return
            "ascent = " + getAscent() +
            " descent = " + getDescent() +
            " linegap = " + getLineGap() +
            " lineheight = " +getLineHeight();
    }
}