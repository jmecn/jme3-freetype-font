package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_Glyph_Metrics;

public class FtGlyphMetrics {
    private final FT_Glyph_Metrics metrics;

    public FtGlyphMetrics(FT_Glyph_Metrics metrics) {
        this.metrics = metrics;
    }

    public long getWidth() {
        return metrics.width();
    }

    public long getHeight() {
        return metrics.height();
    }

    public long getHoriBearingX() {
        return metrics.horiBearingX();
    }

    public long getHoriBearingY() {
        return metrics.horiBearingY();
    }

    public long getHoriAdvance() {
        return metrics.horiAdvance();
    }

    public long getVertBearingX() {
        return metrics.vertBearingX();
    }

    public long getVertBearingY() {
        return metrics.vertBearingY();
    }

    public long getVertAdvance() {
        return metrics.vertAdvance();
    }

    public long getAddress() {
        return metrics.address();
    }
}