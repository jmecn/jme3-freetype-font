package io.github.jmecn.font;

import com.jme3.font.BitmapCharacter;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Glyph extends BitmapCharacter {

    private boolean isFixedWidth;

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

    @Override
    public String toString() {
        return "Glyph{" +
                "char=" + getChar() +
                ", x=" + getX() +
                ", y=" + getY() +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", XOffset=" + getXOffset() +
                ", YOffset=" + getYOffset() +
                ", XAdvance=" + getXAdvance() +
                ", page=" + getPage() +
                ", isFixedWidth=" + isFixedWidth +
                '}';
    }
}
