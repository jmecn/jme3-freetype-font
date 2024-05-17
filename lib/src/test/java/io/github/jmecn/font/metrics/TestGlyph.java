package io.github.jmecn.font.metrics;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.freetype.FtLibrary;

import static org.lwjgl.util.freetype.FreeType.FT_RENDER_MODE_NORMAL;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestGlyph {

    // generate parameters
    char ch;
    String font;
    int pixelSize = 16;
    int renderMode = FT_RENDER_MODE_NORMAL;
    int spread = 2;// in [2, 32], for sdf render
    ColorRGBA color = ColorRGBA.White;
    float gamma = 1.8f;
    Texture.MinFilter minFilter = Texture.MinFilter.NearestNoMipMaps;
    Texture.MagFilter magFilter = Texture.MagFilter.Nearest;


    // font metrics
    private int lineHeight;
    private int maxAdvance;
    private int ascent;
    private int descent;
    private int base;
    private short ppemX;
    private short ppemY;
    private float scaleX;
    private float scaleY;

    // glyph metrics
    private int width;
    private int height;
    private int horiBearingX;
    private int horiBearingY;
    private int horiAdvance;
    private int vertBearingX;
    private int vertBearingY;
    private int vertAdvance;

    private Image image;

    public TestGlyph(String font, int pixelSize, char ch) {
        this.font = font;
        this.pixelSize = pixelSize;
        this.ch = ch;
    }

    public TestGlyph(String font, int pixelSize, char ch, int renderMode) {
        this.font = font;
        this.pixelSize = pixelSize;
        this.ch = ch;
        this.renderMode = renderMode;
    }

    public TestGlyph(String font, int pixelSize, char ch, int renderMode, float gamma) {
        this.font = font;
        this.pixelSize = pixelSize;
        this.ch = ch;
        this.renderMode = renderMode;
    }

    public String getFont() {
        return font;
    }

    public TestGlyph setFont(String font) {
        this.font = font;
        return this;
    }

    public int getPixelSize() {
        return pixelSize;
    }

    public TestGlyph setPixelSize(int pixelSize) {
        this.pixelSize = pixelSize;
        return this;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public TestGlyph setRenderMode(int renderMode) {
        this.renderMode = renderMode;
        return this;
    }

    public int getSpread() {
        return spread;
    }

    public TestGlyph setSpread(int spread) {
        if (spread < FtLibrary.MIN_SPREAD || spread > FtLibrary.MAX_SPREAD) {
            throw new IllegalArgumentException("spread should between [2, 32]");
        }
        this.spread = spread;
        return this;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public TestGlyph setColor(ColorRGBA color) {
        this.color = color;
        return this;
    }

    public float getGamma() {
        return gamma;
    }

    public TestGlyph setGamma(float gamma) {
        this.gamma = gamma;
        return this;
    }

    public Texture.MinFilter getMinFilter() {
        return minFilter;
    }

    public TestGlyph setMinFilter(Texture.MinFilter minFilter) {
        this.minFilter = minFilter;
        return this;
    }

    public Texture.MagFilter getMagFilter() {
        return magFilter;
    }

    public TestGlyph setMagFilter(Texture.MagFilter magFilter) {
        this.magFilter = magFilter;
        return this;
    }

    public char getCh() {
        return ch;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getMaxAdvance() {
        return maxAdvance;
    }

    public void setMaxAdvance(int maxAdvance) {
        this.maxAdvance = maxAdvance;
    }

    public int getAscent() {
        return ascent;
    }

    public void setAscent(int ascent) {
        this.ascent = ascent;
    }

    public int getDescent() {
        return descent;
    }

    public void setDescent(int descent) {
        this.descent = descent;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public short getPpemX() {
        return ppemX;
    }

    public void setPpemX(short ppemX) {
        this.ppemX = ppemX;
    }

    public short getPpemY() {
        return ppemY;
    }

    public void setPpemY(short ppemY) {
        this.ppemY = ppemY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}