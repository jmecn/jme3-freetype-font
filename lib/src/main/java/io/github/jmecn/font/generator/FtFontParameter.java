package io.github.jmecn.font.generator;

import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.Packer;

import java.util.Objects;

import static org.lwjgl.util.freetype.FreeType.*;

public class FtFontParameter {
    public static final String DEFAULT_CHARS = "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";
    public static final int DEFAULT_FONT_SIZE = 16;

    private boolean rightToLeft = false;

    /** The size in pixels */
    private int size = 16;
    private RenderMode renderMode = RenderMode.NORMAL;
    private int spread = 2;
    /** Strength of hinting */
    private Hinting hinting = Hinting.NORMAL;
    /** Foreground color (required for non-black borders) */
    private ColorRGBA color = ColorRGBA.White;
    /** Glyph gamma. Values > 1 reduce antialiasing. */
    private float gamma = 1.8f;
    /** Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph. */
    private int renderCount = 2;
    /** Border width in pixels, 0 to disable */
    private float borderWidth = 0;
    /** Border color; only used if borderWidth > 0 */
    private ColorRGBA borderColor = ColorRGBA.Black;
    /** true for straight (mitered), false for rounded borders */
    private boolean borderStraight = false;
    /** Values < 1 increase the border size. */
    private float borderGamma = 1.8f;
    /** Offset of text shadow on X axis in pixels, 0 to disable */
    private int shadowOffsetX = 0;
    /** Offset of text shadow on Y axis in pixels, 0 to disable */
    private int shadowOffsetY = 0;
    /** Shadow color; only used if shadowOffset > 0. If alpha component is 0, no shadow is drawn but characters are still offset
     * by shadowOffset. */
    private ColorRGBA shadowColor = new ColorRGBA(0, 0, 0, 0.75f);
    /** Pixels to add to glyph spacing when text is rendered. Can be negative. */
    private int spaceX;
    private int spaceY;
    /** Pixels to add to the glyph in the texture. Cannot be negative. */
    private int padTop;
    private int padLeft;
    private int padBottom;
    private int padRight;
    /** The characters the font should contain. If '\0' is not included then {@link FtBitmapCharacterSet#missingGlyph} is not set. */
    private String characters = DEFAULT_CHARS;
    /** Whether the font should include kerning */
    private boolean kerning = true;
    /** The optional Packer to use for packing multiple fonts into a single texture.
     * @see FtFontParameter */
    private Packer packer = null;
    /** Whether to generate mip maps for the resulting texture */
    private boolean genMipMaps = false;
    /** Minification filter */
    private Texture.MinFilter minFilter = Texture.MinFilter.NearestNoMipMaps;
    /** Magnification filter */
    private Texture.MagFilter magFilter = Texture.MagFilter.Bilinear;

    /**
     * Material definition to use for the font.
     */
    private MaterialDef matDef;

    /**
     * Material definition name to use for the font.
     */
    private String matDefName = "Common/MatDefs/Misc/Unshaded.j3md";
    /**
     * Material uniform param name for the color map.
     */
    private String colorMapParamName = "ColorMap";// or DiffuseMap in Lighting.j3md

    /**
     * When true, vertex color is used instead of texture color.
     */
    private boolean useVertexColor = true;
    /**
     * Material uniform param name for the vertex color.
     */
    private String vertexColorParamName = "VertexColor";

    /** When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed. The
     * FreeTypeFontGenerator must not be disposed until the font is no longer needed. The FreeTypeBitmapFontData must be
     * disposed (separately from the generator) when the font is no longer needed. The FreeTypeFontParameter should not be
     * modified after creating a font. If a PixmapPacker is not specified, the font glyph page textures will use
     * {@link FtFontGenerator#MAX_SIZE}. */
    private boolean incremental = false;

    public boolean isRightToLeft() {
        return rightToLeft;
    }

    public void setRightToLeft(boolean rightToLeft) {
        this.rightToLeft = rightToLeft;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Hinting getHinting() {
        return hinting;
    }

    public void setHinting(Hinting hinting) {
        this.hinting = hinting;
    }

    public int getLoadFlags() {
        if (hinting == null) {
            return FT_LOAD_DEFAULT;
        }
        return hinting.getLoadFlags();
    }

    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        // automatically set the texture filter
        switch (renderMode) {
            case MONO:
                this.magFilter = Texture.MagFilter.Nearest;
                this.minFilter = Texture.MinFilter.NearestNoMipMaps;
                break;
            case LIGHT:
            case NORMAL:
                this.magFilter = Texture.MagFilter.Bilinear;
                this.minFilter = Texture.MinFilter.NearestNoMipMaps;
                break;
            case SDF:
                this.magFilter = Texture.MagFilter.Bilinear;
                this.minFilter = Texture.MinFilter.BilinearNoMipMaps;
                break;
        }
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public int getSpread() {
        return spread;
    }

    public void setSpread(int spread) {
        this.spread = spread;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    public float getGamma() {
        return gamma;
    }

    public void setGamma(float gamma) {
        this.gamma = gamma;
    }

    public int getRenderCount() {
        return renderCount;
    }

    public void setRenderCount(int renderCount) {
        this.renderCount = renderCount;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public ColorRGBA getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(ColorRGBA borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isBorderStraight() {
        return borderStraight;
    }

    public void setBorderStraight(boolean borderStraight) {
        this.borderStraight = borderStraight;
    }

    public float getBorderGamma() {
        return borderGamma;
    }

    public void setBorderGamma(float borderGamma) {
        this.borderGamma = borderGamma;
    }

    public int getShadowOffsetX() {
        return shadowOffsetX;
    }

    public void setShadowOffsetX(int shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
    }

    public int getShadowOffsetY() {
        return shadowOffsetY;
    }

    public void setShadowOffsetY(int shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
    }

    public ColorRGBA getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(ColorRGBA shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setSpace(int space) {
        spaceX = spaceY = space;
    }

    public void setSpace(int x, int y) {
        spaceX = x;
        spaceY = y;
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

    public void setPadding(int padding) {
        padTop = padRight = padBottom = padLeft = padding;
    }

    public void setPadding(int top, int right, int bottom, int left) {
        this.padTop = top;
        this.padRight = right;
        this.padBottom = bottom;
        this.padLeft = left;
    }

    public int getPadTop() {
        return padTop;
    }

    public void setPadTop(int padTop) {
        this.padTop = padTop;
    }

    public int getPadLeft() {
        return padLeft;
    }

    public void setPadLeft(int padLeft) {
        this.padLeft = padLeft;
    }

    public int getPadBottom() {
        return padBottom;
    }

    public void setPadBottom(int padBottom) {
        this.padBottom = padBottom;
    }

    public int getPadRight() {
        return padRight;
    }

    public void setPadRight(int padRight) {
        this.padRight = padRight;
    }

    public String getCharacters() {
        return characters;
    }

    public void setCharacters(String characters) {
        this.characters = characters;
    }

    public boolean isKerning() {
        return kerning;
    }

    public void setKerning(boolean kerning) {
        this.kerning = kerning;
    }

    public Packer getPacker() {
        return packer;
    }

    public void setPacker(Packer packer) {
        this.packer = packer;
    }

    public boolean isGenMipMaps() {
        return genMipMaps;
    }

    public void setGenMipMaps(boolean genMipMaps) {
        this.genMipMaps = genMipMaps;
    }

    public Texture.MinFilter getMinFilter() {
        return minFilter;
    }

    public void setMinFilter(Texture.MinFilter minFilter) {
        this.minFilter = minFilter;
    }

    public Texture.MagFilter getMagFilter() {
        return magFilter;
    }

    public void setMagFilter(Texture.MagFilter magFilter) {
        this.magFilter = magFilter;
    }

    public MaterialDef getMatDef() {
        return matDef;
    }

    public void setMatDef(MaterialDef matDef) {
        this.matDef = matDef;
    }

    public String getMatDefName() {
        return matDefName;
    }

    public void setMatDefName(String matDefName) {
        this.matDefName = matDefName;
    }

    public String getColorMapParamName() {
        return colorMapParamName;
    }

    public void setColorMapParamName(String colorMapParamName) {
        this.colorMapParamName = colorMapParamName;
    }

    public boolean isUseVertexColor() {
        return useVertexColor;
    }

    public void setUseVertexColor(boolean useVertexColor) {
        this.useVertexColor = useVertexColor;
    }

    public String getVertexColorParamName() {
        return vertexColorParamName;
    }

    public void setVertexColorParamName(String vertexColorParamName) {
        this.vertexColorParamName = vertexColorParamName;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FtFontParameter)) {
            return false;
        }
        FtFontParameter parameter = (FtFontParameter) o;
        return size == parameter.size && renderMode == parameter.renderMode && spread == parameter.spread && Float.compare(gamma, parameter.gamma) == 0 && renderCount == parameter.renderCount && Float.compare(borderWidth, parameter.borderWidth) == 0 && borderStraight == parameter.borderStraight && Float.compare(borderGamma, parameter.borderGamma) == 0 && shadowOffsetX == parameter.shadowOffsetX && shadowOffsetY == parameter.shadowOffsetY && spaceX == parameter.spaceX && spaceY == parameter.spaceY && padTop == parameter.padTop && padLeft == parameter.padLeft && padBottom == parameter.padBottom && padRight == parameter.padRight && kerning == parameter.kerning && genMipMaps == parameter.genMipMaps && useVertexColor == parameter.useVertexColor && incremental == parameter.incremental && hinting == parameter.hinting && Objects.equals(color, parameter.color) && Objects.equals(borderColor, parameter.borderColor) && Objects.equals(shadowColor, parameter.shadowColor) && Objects.equals(characters, parameter.characters) && Objects.equals(packer, parameter.packer) && minFilter == parameter.minFilter && magFilter == parameter.magFilter && Objects.equals(matDef, parameter.matDef) && Objects.equals(matDefName, parameter.matDefName) && Objects.equals(colorMapParamName, parameter.colorMapParamName) && Objects.equals(vertexColorParamName, parameter.vertexColorParamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, renderMode, spread, hinting, color, gamma, renderCount, borderWidth, borderColor, borderStraight, borderGamma, shadowOffsetX, shadowOffsetY, shadowColor, spaceX, spaceY, padTop, padLeft, padBottom, padRight, characters, kerning, packer, genMipMaps, minFilter, magFilter, matDef, matDefName, colorMapParamName, useVertexColor, vertexColorParamName, incremental);
    }
}
