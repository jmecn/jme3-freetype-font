package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Glyph extends BitmapCharacter {

    private boolean isFixedWidth;

    float u, v;
    float u2, v2;

    public Glyph() {
        super();
    }
    public Glyph(char c) {
        super(c);
    }

    public boolean isFixedWidth() {
        return isFixedWidth;
    }

    public void setFixedWidth(boolean isFixedWidth) {
        this.isFixedWidth = isFixedWidth;
    }
}
