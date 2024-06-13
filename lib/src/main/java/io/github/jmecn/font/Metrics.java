package io.github.jmecn.font;

public interface Metrics {
    /* These are all user space values */
    public float getAscent();
    public float getDescent();
    public float getLineGap();
    public float getLineHeight();
    public float getTypoAscent();
    public float getTypoDescent();
    public float getTypoLineGap();
    public float getXHeight();
    public float getCapHeight();
    public float getStrikethroughOffset();
    public float getStrikethroughThickness();
    public float getUnderLineOffset();
    public float getUnderLineThickness();
}
