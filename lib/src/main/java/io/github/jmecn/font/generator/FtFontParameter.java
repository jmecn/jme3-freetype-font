package io.github.jmecn.font.generator;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.packer.Packer;

public class FtFontParameter {
    public static final String DEFAULT_CHARS = "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";

    /** The size in pixels */
    public int size = 16;
    /** If true, font smoothing is disabled. */
    public boolean mono;
    /** Strength of hinting */
    public Hinting hinting = Hinting.NORMAL;
    /** Foreground color (required for non-black borders) */
    public ColorRGBA color = ColorRGBA.White;
    /** Glyph gamma. Values > 1 reduce antialiasing. */
    public float gamma = 1.8f;
    /** Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph. */
    public int renderCount = 2;
    /** Border width in pixels, 0 to disable */
    public float borderWidth = 0;
    /** Border color; only used if borderWidth > 0 */
    public ColorRGBA borderColor = ColorRGBA.Black;
    /** true for straight (mitered), false for rounded borders */
    public boolean borderStraight = false;
    /** Values < 1 increase the border size. */
    public float borderGamma = 1.8f;
    /** Offset of text shadow on X axis in pixels, 0 to disable */
    public int shadowOffsetX = 0;
    /** Offset of text shadow on Y axis in pixels, 0 to disable */
    public int shadowOffsetY = 0;
    /** Shadow color; only used if shadowOffset > 0. If alpha component is 0, no shadow is drawn but characters are still offset
     * by shadowOffset. */
    public ColorRGBA shadowColor = new ColorRGBA(0, 0, 0, 0.75f);
    /** Pixels to add to glyph spacing when text is rendered. Can be negative. */
    public int spaceX;
    public int spaceY;
    /** Pixels to add to the glyph in the texture. Cannot be negative. */
    public int padTop;
    public int padLeft;
    public int padBottom;
    public int padRight;
    /** The characters the font should contain. If '\0' is not included then {@link BitmapFontData#missingGlyph} is not set. */
    public String characters = DEFAULT_CHARS;
    /** Whether the font should include kerning */
    public boolean kerning = true;
    /** The optional Packer to use for packing multiple fonts into a single texture.
     * @see FtFontParameter */
    public Packer packer = null;
    /** Whether to flip the font vertically */
    public boolean flip = false;
    /** Whether to generate mip maps for the resulting texture */
    public boolean genMipMaps = false;
    /** Minification filter */
    public Texture.MinFilter minFilter = Texture.MinFilter.NearestNoMipMaps;
    /** Magnification filter */
    public Texture.MagFilter magFilter = Texture.MagFilter.Nearest;
    /** When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed. The
     * FreeTypeFontGenerator must not be disposed until the font is no longer needed. The FreeTypeBitmapFontData must be
     * disposed (separately from the generator) when the font is no longer needed. The FreeTypeFontParameter should not be
     * modified after creating a font. If a PixmapPacker is not specified, the font glyph page textures will use
     * {@link FtFontGenerator#MAX_SIZE}. */
    public boolean incremental;

    public Packer getPacker() {
        return packer;
    }
}
