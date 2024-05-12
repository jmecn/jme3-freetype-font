package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_Size;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/11
 */
public class FtSize {
    private final FT_Size size;
    private FtSizeMetrics metrics;
    public FtSize(FT_Size size) {
        this.size = size;
    }

    public FtSizeMetrics getMetrics() {
        if (metrics == null) {
            metrics = new FtSizeMetrics(size.metrics());
        }
        return metrics;
    }
}
