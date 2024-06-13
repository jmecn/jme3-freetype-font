package io.github.jmecn.text;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TextLine {
    private TextRun[] runs;
    private RectBounds bounds;
    private float lsb;
    private float rsb;
    private float leading;
    private int start;
    private int length;

    public TextLine(int start, int length, TextRun[] runs,
                    float width, float ascent, float descent, float leading) {
        this.start = start;
        this.length = length;
        this.bounds = new RectBounds(0, ascent, width, descent + leading);
        this.leading = leading;
        this.runs = runs;
    }

    /**
     * Returns metrics information about the line as follow:
     *
     * bounds().getWidth() - the width of the line.
     * The width for the line is sum of all run's width in the line, it is not
     * affect by any wrapping width but it will include any changes caused by
     * justification.
     *
     * bounds().getHeight() - the height of the line.
     * The height of the line is sum of the max ascent, max descent, and
     * max line gap of all the fonts in the line.
     *
     * bounds.().getMinY() - the ascent of the line (negative).
     * The ascent of the line is the max ascent of all fonts in the line.
     *
     * bounds().getMinX() - the x origin of the line (relative to the layout).
     * The x origin is defined by TextAlignment of the text layout, always zero
     * for left-aligned text.
     */
    public RectBounds getBounds() {
        return bounds;
    }

    public float getLeading() {
        return leading;
    }

    /**
     * Returns the list of GlyphList in the line. The list is visually orderded.
     */
    public TextRun[] getRuns() {
        return runs;
    }

    /**
     * Returns the line start offset.
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the line length in character.
     */
    public int getLength() {
        return length;
    }

    public void setSideBearings(float lsb, float rsb) {
        this.lsb = lsb;
        this.rsb = rsb;
    }

    /**
     * Returns the left side bearing of the line (negative).
     */
    public float getLeftSideBearing() {
        return lsb;
    }

    /**
     * Returns the right side bearing of the line (positive).
     */
    public float getRightSideBearing() {
        return rsb;
    }

    public void setAlignment(float x) {
        bounds.setMinX(x);
        bounds.setMaxX(x + bounds.getMaxX());
    }

    public void setWidth(float width) {
        bounds.setMaxX(bounds.getMinX() + width);
    }
}
