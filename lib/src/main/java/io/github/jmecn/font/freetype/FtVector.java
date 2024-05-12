package io.github.jmecn.font.freetype;

import org.lwjgl.util.freetype.FT_Vector;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtVector {
    public long x;
    public long y;

    public FtVector() {
        x = y = 0;
    }

    public FtVector(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public FtVector(FT_Vector vector) {
        this.x = vector.x();
        this.y = vector.y();
    }
}
