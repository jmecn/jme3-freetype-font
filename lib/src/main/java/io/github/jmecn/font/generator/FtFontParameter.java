package io.github.jmecn.font.generator;

import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.CommonChars;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.Direction;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

import static org.lwjgl.util.freetype.FreeType.*;

public class FtFontParameter {
    public static final int DEFAULT_FONT_SIZE = 16;

    ////// keys to be stored in the preset file //////
    public static final String FONT_FILE = "font.file";
    public static final String FONT_SIZE = "font.size";
    public static final String FONT_KERNING = "font.kerning";
    public static final String FONT_INCREMENTAL = "font.incremental";
    public static final String PACK_WIDTH = "pack.width";
    public static final String PACK_HEIGHT = "pack.height";
    public static final String PACK_PADDING = "pack.padding";
    public static final String PACK_STRATEGY = "pack.strategy";
    public static final String RENDER_HINTING = "render.hinting";
    public static final String RENDER_MODE = "render.mode";
    public static final String RENDER_SPREAD = "render.spread";
    public static final String RENDER_COLOR = "render.color";
    public static final String RENDER_GAMMA = "render.gamma";
    public static final String RENDER_COUNT = "render.count";
    public static final String BORDER_WIDTH = "border.width";
    public static final String BORDER_COLOR = "border.color";
    public static final String BORDER_GAMMA = "border.gamma";
    public static final String BORDER_STRAIGHT = "border.straight";
    public static final String SHADOW_OFFSET_X = "shadow.offsetX";
    public static final String SHADOW_OFFSET_Y = "shadow.offsetY";
    public static final String SHADOW_COLOR = "shadow.color";
    public static final String SPACE_X = "space.x";
    public static final String SPACE_Y = "space.y";
    public static final String PADDING_LEFT = "padding.left";
    public static final String PADDING_RIGHT = "padding.right";
    public static final String PADDING_TOP = "padding.top";
    public static final String PADDING_BOTTOM = "padding.bottom";
    public static final String MATERIAL_DEFINE = "material.define";
    public static final String MATERIAL_COLOR_MAP = "material.colorMap";
    public static final String MATERIAL_VERTEX_COLOR = "material.vertexColor";
    public static final String MATERIAL_USE_VERTEX_COLOR = "material.useVertexColor";
    public static final String TEXTURE_MIN_FILTER = "texture.minFilter";
    public static final String TEXTURE_MAG_FILTER = "texture.magFilter";
    //////////////////////////////////////////////////

    private Direction direction = Direction.LTR;

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
    private int borderWidth = 0;
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
    private String characters = CommonChars.ASCII.getChars();
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
     * {@link FtFontGenerator#maxTextureSize}. */
    private boolean incremental = false;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
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

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
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
        return size == parameter.size && renderMode == parameter.renderMode && spread == parameter.spread && Float.compare(gamma, parameter.gamma) == 0 && renderCount == parameter.renderCount && borderWidth == parameter.borderWidth && borderStraight == parameter.borderStraight && Float.compare(borderGamma, parameter.borderGamma) == 0 && shadowOffsetX == parameter.shadowOffsetX && shadowOffsetY == parameter.shadowOffsetY && spaceX == parameter.spaceX && spaceY == parameter.spaceY && padTop == parameter.padTop && padLeft == parameter.padLeft && padBottom == parameter.padBottom && padRight == parameter.padRight && kerning == parameter.kerning && genMipMaps == parameter.genMipMaps && useVertexColor == parameter.useVertexColor && incremental == parameter.incremental && hinting == parameter.hinting && Objects.equals(color, parameter.color) && Objects.equals(borderColor, parameter.borderColor) && Objects.equals(shadowColor, parameter.shadowColor) && Objects.equals(characters, parameter.characters) && Objects.equals(packer, parameter.packer) && minFilter == parameter.minFilter && magFilter == parameter.magFilter && Objects.equals(matDef, parameter.matDef) && Objects.equals(matDefName, parameter.matDefName) && Objects.equals(colorMapParamName, parameter.colorMapParamName) && Objects.equals(vertexColorParamName, parameter.vertexColorParamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, renderMode, spread, hinting, color, gamma, renderCount, borderWidth, borderColor, borderStraight, borderGamma, shadowOffsetX, shadowOffsetY, shadowColor, spaceX, spaceY, padTop, padLeft, padBottom, padRight, characters, kerning, packer, genMipMaps, minFilter, magFilter, matDef, matDefName, colorMapParamName, useVertexColor, vertexColorParamName, incremental);
    }

    public void loadProperties(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            return;
        }

        loadProperties(properties);
    }

    public void loadProperties(Properties properties) {
        if (properties.containsKey(PACK_WIDTH) && properties.containsKey(PACK_HEIGHT) || properties.containsKey(PACK_PADDING)) {
            int width = getInt(PACK_WIDTH, properties);
            int height = getInt(PACK_HEIGHT, properties);
            int padding = getInt(PACK_PADDING, properties);
            PackStrategy packStrategy;
            if (properties.containsKey(PACK_STRATEGY)) {
                String strategy = getString(PACK_STRATEGY, properties);
                if (SkylineStrategy.class.getSimpleName().equals(strategy)) {
                    packStrategy = new SkylineStrategy();
                } else {
                    packStrategy = new GuillotineStrategy();
                }
            } else {
                packStrategy = null;
            }
            this.packer = new Packer(Image.Format.RGBA8, width, height, padding, false, packStrategy);
        }

        if (properties.containsKey(FONT_SIZE)) {
            this.setSize(getInt(FONT_SIZE, properties));
        }
        if (properties.containsKey(FONT_KERNING)) {
            this.setKerning(getBool(FONT_KERNING, properties));
        }
        if (properties.containsKey(FONT_INCREMENTAL)) {
            this.setIncremental(getBool(FONT_INCREMENTAL, properties));
        }
        if (properties.containsKey(RENDER_HINTING)) {
            this.setHinting(Hinting.valueOf(getString(RENDER_HINTING, properties)));
        }
        if (properties.containsKey(RENDER_MODE)) {
            this.setRenderMode(RenderMode.valueOf(getString(RENDER_MODE, properties)));
        }
        if (properties.containsKey(RENDER_SPREAD)) {
            this.setSpread(getInt(RENDER_SPREAD, properties));
        }
        if (properties.containsKey(RENDER_COLOR)) {
            this.setColor(getRGBA(RENDER_COLOR, properties));
        }
        if (properties.containsKey(RENDER_GAMMA)) {
            this.setGamma(getFloat(RENDER_GAMMA, properties));
        }
        if (properties.containsKey(RENDER_COUNT)) {
            this.setRenderCount(getInt(RENDER_COUNT, properties));
        }

        if (properties.containsKey(BORDER_WIDTH)) {
            this.setBorderWidth(getInt(BORDER_WIDTH, properties));
        }
        if (properties.containsKey(BORDER_COLOR)) {
            this.setBorderColor(getRGBA(BORDER_COLOR, properties));
        }
        if (properties.containsKey(BORDER_GAMMA)) {
            this.setBorderGamma(getFloat(BORDER_GAMMA, properties));
        }
        if (properties.containsKey(BORDER_STRAIGHT)) {
            this.setBorderStraight(getBool(BORDER_STRAIGHT, properties));
        }

        if (properties.containsKey(SHADOW_OFFSET_X)) {
            this.setShadowOffsetX(getInt(SHADOW_OFFSET_X, properties));
        }
        if (properties.containsKey(SHADOW_OFFSET_Y)) {
            this.setShadowOffsetY(getInt(SHADOW_OFFSET_Y, properties));
        }
        if (properties.containsKey(SHADOW_COLOR)) {
            this.setShadowColor(getRGBA(SHADOW_COLOR, properties));
        }

        if (properties.containsKey(SPACE_X)) {
            this.setSpaceX(getInt(SPACE_X, properties));
        }
        if (properties.containsKey(SPACE_Y)) {
            this.setSpaceY(getInt(SPACE_Y, properties));
        }

        if (properties.containsKey(PADDING_LEFT) || properties.containsKey(PADDING_RIGHT) || properties.containsKey(PADDING_TOP) || properties.containsKey(PADDING_BOTTOM)) {
            this.setPadding(getInt(PADDING_TOP, properties), getInt(PADDING_RIGHT, properties), getInt(PADDING_BOTTOM, properties), getInt(PADDING_LEFT, properties));
        }

        if (properties.containsKey(TEXTURE_MIN_FILTER)) {
            this.setMinFilter(Texture.MinFilter.valueOf(getString(TEXTURE_MIN_FILTER, properties)));
        }
        if (properties.containsKey(TEXTURE_MAG_FILTER)) {
            this.setMagFilter(Texture.MagFilter.valueOf(getString(TEXTURE_MAG_FILTER, properties)));
        }

        if (properties.containsKey(MATERIAL_DEFINE)) {
            this.setMatDefName(getString(MATERIAL_DEFINE, properties));
        }
        if (properties.containsKey(MATERIAL_COLOR_MAP)) {
            this.setColorMapParamName(getString(MATERIAL_COLOR_MAP, properties));
        }
        if (properties.containsKey(MATERIAL_VERTEX_COLOR)) {
            this.setVertexColorParamName(getString(MATERIAL_VERTEX_COLOR, properties));
        }
        if (properties.containsKey(MATERIAL_USE_VERTEX_COLOR)) {
            this.setUseVertexColor(getBool(MATERIAL_USE_VERTEX_COLOR, properties));
        }
    }

    public void saveToProperties(Properties properties) {
        if (packer != null) {
            properties.setProperty(PACK_WIDTH, String.valueOf(packer.getPageWidth()));
            properties.setProperty(PACK_HEIGHT, String.valueOf(packer.getPageHeight()));
            properties.setProperty(PACK_PADDING, String.valueOf(packer.getPadding()));
            properties.setProperty(PACK_STRATEGY, packer.getPackStrategy().getClass().getSimpleName());
        }

        properties.setProperty(FONT_SIZE, String.valueOf(this.getSize()));
        properties.setProperty(FONT_KERNING, String.valueOf(this.isKerning()));
        properties.setProperty(FONT_INCREMENTAL, String.valueOf(this.isIncremental()));

        properties.setProperty(RENDER_HINTING,this.getHinting().name());
        properties.setProperty(RENDER_MODE, this.getRenderMode().name());
        properties.setProperty(RENDER_SPREAD, String.valueOf(this.getSpread()));
        properties.setProperty(RENDER_COLOR, String.format("%08X", this.getColor().asIntRGBA()));
        properties.setProperty(RENDER_GAMMA, String.valueOf(this.getGamma()));
        properties.setProperty(RENDER_COUNT, String.valueOf(this.getRenderCount()));

        properties.setProperty(BORDER_WIDTH, String.valueOf(this.getBorderWidth()));
        properties.setProperty(BORDER_COLOR, String.format("%08X", this.getBorderColor().asIntRGBA()));
        properties.setProperty(BORDER_GAMMA, String.valueOf(this.getBorderGamma()));
        properties.setProperty(BORDER_STRAIGHT, String.valueOf(this.isBorderStraight()));

        properties.setProperty(SHADOW_OFFSET_X, String.valueOf(this.getShadowOffsetX()));
        properties.setProperty(SHADOW_OFFSET_Y, String.valueOf(this.getShadowOffsetY()));
        properties.setProperty(SHADOW_COLOR, String.format("%08X", this.getShadowColor().asIntRGBA()));

        properties.setProperty(SPACE_X, String.valueOf(this.getSpaceX()));
        properties.setProperty(SPACE_Y, String.valueOf(this.getSpaceY()));

        properties.setProperty(PADDING_LEFT, String.valueOf(this.getPadLeft()));
        properties.setProperty(PADDING_RIGHT, String.valueOf(this.getPadRight()));
        properties.setProperty(PADDING_TOP, String.valueOf(this.getPadTop()));
        properties.setProperty(PADDING_BOTTOM, String.valueOf(this.getPadBottom()));

        properties.setProperty(TEXTURE_MIN_FILTER, this.getMinFilter().name());
        properties.setProperty(TEXTURE_MAG_FILTER, this.getMagFilter().name());

        properties.setProperty(MATERIAL_DEFINE, this.getMatDefName());
        properties.setProperty(MATERIAL_COLOR_MAP, this.getColorMapParamName());
        properties.setProperty(MATERIAL_VERTEX_COLOR, this.getVertexColorParamName());
        properties.setProperty(MATERIAL_USE_VERTEX_COLOR, String.valueOf(this.isUseVertexColor()));
    }

    private static String getString(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return value;
    }

    private static int getInt(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private static float getFloat(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return 0.0f;
        }
        return Float.parseFloat(value);
    }

    private static boolean getBool(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private static ColorRGBA getRGBA(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return ColorRGBA.BlackNoAlpha;
        }
        int c = (int) Long.parseLong(value, 16);
        int red = (c >> 24) & 0xFF;
        int green = (c >> 16) & 0xFF;
        int blue = (c >> 8) & 0xFF;
        int alpha = c & 0xFF;
        return ColorRGBA.fromRGBA255(red, green, blue, alpha);
    }
}
