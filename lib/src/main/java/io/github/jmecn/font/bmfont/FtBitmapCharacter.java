package io.github.jmecn.font.bmfont;

import com.jme3.font.BitmapCharacter;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtBitmapCharacter extends BitmapCharacter {

    private int yAdvance;
    // font format properties
    private boolean isFixedWidth;

    // bitmap position
    private int left;
    private int top;

    // glyph metrics
    private int horiBearingX;
    private int horiBearingY;
    private int horiAdvance;
    private int vertBearingX;
    private int vertBearingY;
    private int vertAdvance;

    // generator parameter
    private int borderWidth;
    private int spaceX;
    private int spaceY;
    public FtBitmapCharacter() {
        super();
    }
    public FtBitmapCharacter(char c) {
        super(c);
    }

    public int getYAdvance() {
        return yAdvance;
    }

    public void setYAdvance(int yAdvance) {
        this.yAdvance = yAdvance;
    }

    public boolean isFixedWidth() {
        return isFixedWidth;
    }

    public void setFixedWidth(boolean isFixedWidth) {
        this.isFixedWidth = isFixedWidth;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getHoriBearingX() {
        return horiBearingX;
    }

    public void setHoriBearingX(int horiBearingX) {
        this.horiBearingX = horiBearingX;
    }

    public int getHoriBearingY() {
        return horiBearingY;
    }

    public void setHoriBearingY(int horiBearingY) {
        this.horiBearingY = horiBearingY;
    }

    public int getHoriAdvance() {
        return horiAdvance;
    }

    public void setHoriAdvance(int horiAdvance) {
        this.horiAdvance = horiAdvance;
    }

    public int getVertBearingX() {
        return vertBearingX;
    }

    public void setVertBearingX(int vertBearingX) {
        this.vertBearingX = vertBearingX;
    }

    public int getVertBearingY() {
        return vertBearingY;
    }

    public void setVertBearingY(int vertBearingY) {
        this.vertBearingY = vertBearingY;
    }

    public int getVertAdvance() {
        return vertAdvance;
    }

    public void setVertAdvance(int vertAdvance) {
        this.vertAdvance = vertAdvance;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public int getSpaceX() {
        return spaceX;
    }

    public void setSpaceX(int spaceX) {
        this.spaceX = spaceX;
    }

    public int getSpaceY() {
        return spaceY;
    }

    public void setSpaceY(int spaceY) {
        this.spaceY = spaceY;
    }

    @Override
    public String toString() {
        return "FtBitmapCharacter{" +
                "char=" + getChar() +
                ", x=" + getX() +
                ", y=" + getY() +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", XOffset=" + getXOffset() +
                ", YOffset=" + getYOffset() +
                ", XAdvance=" + getXAdvance() +
                ", YAdvance=" + getYAdvance() +
                ", page=" + getPage() +
                ", isFixedWidth=" + isFixedWidth +
                ", left=" + left +
                ", top=" + top +
                ", horiBearingX=" + horiBearingX +
                ", horiBearingY=" + horiBearingY +
                ", horiAdvance=" + horiAdvance +
                ", vertBearingX=" + vertBearingX +
                ", vertBearingY=" + vertBearingY +
                ", vertAdvance=" + vertAdvance +
                ", borderWidth=" + borderWidth +
                ", spaceX=" + spaceX +
                ", spaceY=" + spaceY +
                '}';
    }

}
