package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Rectangle;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Scanline {
    protected int x;
    protected int y;
    protected int height;

    public Scanline(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
    }

    public void add(Rectangle rect) {
        rect.setLocation(x, y);
        x += rect.getWidth();
    }
}
