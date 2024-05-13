package io.github.jmecn.font.packer.strategy;

import io.github.jmecn.font.packer.Rectangle;

public class Row {
    protected int x;
    protected int y;
    protected int height;

    public Row(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
    }

    public void add(Rectangle rect, int padding) {
        rect.setLocation(x, y);
        x += rect.getWidth() + padding;
    }
}
