package io.github.jmecn.font.plugins;

import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapFont;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.packer.Packer;

import java.util.Objects;

/**
 * A delegate class to set FtFontParameter.
 *
 * @author yanmaoyuan
 */
public class FtFontKey extends AssetKey<BitmapFont> {

    private final FtFontParameter delegate;

    public FtFontKey() {
        delegate = new FtFontParameter();
    }

    public FtFontKey(String name) {
        this(name, FtFontParameter.DEFAULT_FONT_SIZE, false);
    }

    public FtFontKey(String name, int size) {
        this(name, size, false);
    }

    public FtFontKey(String name, int size, boolean incremental) {
        super(name);
        delegate = new FtFontParameter();
        setSize(size);
        setIncremental(incremental);
    }

    public FtFontParameter getDelegate() {
        return delegate;
    }

    /// delegate method ///

    public boolean isRightToLeft() {
        return delegate.isRightToLeft();
    }

    public void setRightToLeft(boolean rightToLeft) {
        delegate.setRightToLeft(rightToLeft);
    }

    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(int size) {
        delegate.setSize(size);
    }

    public boolean isMono() {
        return delegate.isMono();
    }

    public void setMono(boolean mono) {
        delegate.setMono(mono);
    }

    public Hinting getHinting() {
        return delegate.getHinting();
    }

    public void setHinting(Hinting hinting) {
        delegate.setHinting(hinting);
    }

    public int getLoadFlags() {
        return delegate.getLoadFlags();
    }

    public int getRenderMode() {
        return delegate.getRenderMode();
    }

    public ColorRGBA getColor() {
        return delegate.getColor();
    }

    public void setColor(ColorRGBA color) {
        delegate.setColor(color);
    }

    public float getGamma() {
        return delegate.getGamma();
    }

    public void setGamma(float gamma) {
        delegate.setGamma(gamma);
    }

    public int getRenderCount() {
        return delegate.getRenderCount();
    }

    public void setRenderCount(int renderCount) {
        delegate.setRenderCount(renderCount);
    }

    public float getBorderWidth() {
        return delegate.getBorderWidth();
    }

    public void setBorderWidth(float borderWidth) {
        delegate.setBorderWidth(borderWidth);
    }

    public ColorRGBA getBorderColor() {
        return delegate.getBorderColor();
    }

    public void setBorderColor(ColorRGBA borderColor) {
        delegate.setBorderColor(borderColor);
    }

    public boolean isBorderStraight() {
        return delegate.isBorderStraight();
    }

    public void setBorderStraight(boolean borderStraight) {
        delegate.setBorderStraight(borderStraight);
    }

    public float getBorderGamma() {
        return delegate.getBorderGamma();
    }

    public void setBorderGamma(float borderGamma) {
        delegate.setBorderGamma(borderGamma);
    }

    public int getShadowOffsetX() {
        return delegate.getShadowOffsetX();
    }

    public void setShadowOffsetX(int shadowOffsetX) {
        delegate.setShadowOffsetX(shadowOffsetX);
    }

    public int getShadowOffsetY() {
        return delegate.getShadowOffsetY();
    }

    public void setShadowOffsetY(int shadowOffsetY) {
        delegate.setShadowOffsetY(shadowOffsetY);
    }

    public ColorRGBA getShadowColor() {
        return delegate.getShadowColor();
    }

    public void setShadowColor(ColorRGBA shadowColor) {
        delegate.setShadowColor(shadowColor);
    }

    public void setSpace(int space) {
        delegate.setSpace(space);
    }

    public void setSpace(int x, int y) {
        delegate.setSpace(x, y);
    }

    public int getSpaceX() {
        return delegate.getSpaceX();
    }

    public void setSpaceX(int spaceX) {
        delegate.setSpaceX(spaceX);
    }

    public int getSpaceY() {
        return delegate.getSpaceY();
    }

    public void setSpaceY(int spaceY) {
        delegate.setSpaceY(spaceY);
    }

    public void setPadding(int padding) {
        delegate.setPadding(padding);
    }

    public void setPadding(int top, int right, int bottom, int left) {
        delegate.setPadding(top, right, bottom, left);
    }

    public int getPadTop() {
        return delegate.getPadTop();
    }

    public void setPadTop(int padTop) {
        delegate.setPadTop(padTop);
    }

    public int getPadLeft() {
        return delegate.getPadLeft();
    }

    public void setPadLeft(int padLeft) {
        delegate.setPadLeft(padLeft);
    }

    public int getPadBottom() {
        return delegate.getPadBottom();
    }

    public void setPadBottom(int padBottom) {
        delegate.setPadBottom(padBottom);
    }

    public int getPadRight() {
        return delegate.getPadRight();
    }

    public void setPadRight(int padRight) {
        delegate.setPadRight(padRight);
    }

    public String getCharacters() {
        return delegate.getCharacters();
    }

    public void setCharacters(String characters) {
        delegate.setCharacters(characters);
    }

    public boolean isKerning() {
        return delegate.isKerning();
    }

    public void setKerning(boolean kerning) {
        delegate.setKerning(kerning);
    }

    public Packer getPacker() {
        return delegate.getPacker();
    }

    public void setPacker(Packer packer) {
        delegate.setPacker(packer);
    }

    public boolean isGenMipMaps() {
        return delegate.isGenMipMaps();
    }

    public void setGenMipMaps(boolean genMipMaps) {
        delegate.setGenMipMaps(genMipMaps);
    }

    public Texture.MinFilter getMinFilter() {
        return delegate.getMinFilter();
    }

    public void setMinFilter(Texture.MinFilter minFilter) {
        delegate.setMinFilter(minFilter);
    }

    public Texture.MagFilter getMagFilter() {
        return delegate.getMagFilter();
    }

    public void setMagFilter(Texture.MagFilter magFilter) {
        delegate.setMagFilter(magFilter);
    }

    public MaterialDef getMatDef() {
        return delegate.getMatDef();
    }

    public void setMatDef(MaterialDef matDef) {
        delegate.setMatDef(matDef);
    }

    public String getMatDefName() {
        return delegate.getMatDefName();
    }

    public void setMatDefName(String matDefName) {
        delegate.setMatDefName(matDefName);
    }

    public String getColorMapParamName() {
        return delegate.getColorMapParamName();
    }

    public void setColorMapParamName(String colorMapParamName) {
        delegate.setColorMapParamName(colorMapParamName);
    }

    public boolean isUseVertexColor() {
        return delegate.isUseVertexColor();
    }

    public void setUseVertexColor(boolean useVertexColor) {
        delegate.setUseVertexColor(useVertexColor);
    }

    public String getVertexColorParamName() {
        return delegate.getVertexColorParamName();
    }

    public void setVertexColorParamName(String vertexColorParamName) {
        delegate.setVertexColorParamName(vertexColorParamName);
    }

    public boolean isIncremental() {
        return delegate.isIncremental();
    }

    public void setIncremental(boolean incremental) {
        delegate.setIncremental(incremental);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FtFontKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(delegate, ((FtFontKey) o).delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), delegate);
    }
}