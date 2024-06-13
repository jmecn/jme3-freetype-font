package io.github.jmecn.text;

import io.github.jmecn.font.Font;

class LayoutCache {
    int[] glyphs;
    float[] advances;
    boolean valid;
    int analysis;
    char[] text;
    Font font;
    TextRun[] runs;
    int runCount;
    TextLine[] lines;
    float layoutWidth, layoutHeight;
}
