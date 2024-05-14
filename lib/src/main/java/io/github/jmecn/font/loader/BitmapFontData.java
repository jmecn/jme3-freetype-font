package io.github.jmecn.font.loader;

import com.jme3.font.BitmapCharacter;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.generator.Glyph;
import io.github.jmecn.font.generator.GlyphRun;
import io.github.jmecn.font.packer.TextureRegion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class BitmapFontData {
    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
    private static final int PAGES = 0x10000 / PAGE_SIZE;

    /** The name of the font, or null. */
    public String name;
    /** An array of the image paths, for multiple texture pages. */
    public String[] imagePaths;
    public File fontFile;
    public boolean flipped;
    public float padTop, padRight, padBottom, padLeft;
    /** The distance from one line of text to the next. To set this value, use {@link #setLineHeight(float)}. */
    public float lineHeight;
    /** The distance from the top of most uppercase characters to the baseline. Since the drawing position is the cap height of
     * the first line, the cap height can be used to get the location of the baseline. */
    public float capHeight = 1;
    /** The distance from the cap height to the top of the tallest glyph. */
    public float ascent;
    /** The distance from the bottom of the glyph that extends the lowest to the baseline. This number is negative. */
    public float descent;
    /** The distance to move down when \n is encountered. */
    public float down;
    /** Multiplier for the line height of blank lines. down * blankLineHeight is used as the distance to move down for a blank
     * line. */
    public float blankLineScale = 1;
    public float scaleX = 1, scaleY = 1;
    public boolean markupEnabled;
    /** The amount to add to the glyph X position when drawing a cursor between glyphs. This field is not set by the BMFont
     * file, it needs to be set manually depending on how the glyphs are rendered on the backing textures. */
    public float cursorX;

    public final Glyph[][] glyphs = new Glyph[PAGES][];
    /** The glyph to display for characters not in the font. May be null. */
    public Glyph missingGlyph;

    /** The width of the space character. */
    public float spaceXadvance;
    /** The x-height, which is the distance from the top of most lowercase characters to the baseline. */
    public float xHeight = 1;

    /** Additional characters besides whitespace where text is wrapped. Eg, a hypen (-). */
    public char[] breakChars;
    public char[] xChars = {'x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z'};
    public char[] capChars = {'M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A', 'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public BitmapFontData () {
    }

    /** Sets the line height, which is the distance from one line of text to the next. */
    public void setLineHeight (float height) {
        lineHeight = height * scaleY;
        down = flipped ? lineHeight : -lineHeight;
    }

    public void setGlyph (int ch, Glyph glyph) {
        Glyph[] page = glyphs[ch / PAGE_SIZE];
        if (page == null) glyphs[ch / PAGE_SIZE] = page = new Glyph[PAGE_SIZE];
        page[ch & PAGE_SIZE - 1] = glyph;
    }

    public Glyph getFirstGlyph () {
        for (Glyph[] page : this.glyphs) {
            if (page == null) continue;
            for (Glyph glyph : page) {
                if (glyph == null || glyph.getHeight() == 0 || glyph.getWidth() == 0) continue;
                return glyph;
            }
        }
        throw new FtRuntimeException("No glyphs found.");
    }

    /** Returns true if the font has the glyph, or if the font has a {@link #missingGlyph}. */
    public boolean hasGlyph (char ch) {
        if (missingGlyph != null) return true;
        return getGlyph(ch) != null;
    }

    /** Returns the glyph for the specified character, or null if no such glyph exists. Note that
     * {@link #getGlyphs(GlyphRun, CharSequence, int, int, Glyph)} should be be used to shape a string of characters into a list
     * of glyphs. */
    public Glyph getGlyph (char ch) {
        Glyph[] page = glyphs[ch / PAGE_SIZE];
        if (page != null) return page[ch & PAGE_SIZE - 1];
        return null;
    }

    /** Using the specified string, populates the glyphs and positions of the specified glyph run.
     * @param str Characters to convert to glyphs. Will not contain newline or color tags. May contain "[[" for an escaped left
     *           square bracket.
     * @param lastGlyph The glyph immediately before this run, or null if this is run is the first on a line of text. Used tp
     *           apply kerning between the specified glyph and the first glyph in this run. */
    public void getGlyphs (GlyphRun run, CharSequence str, int start, int end, Glyph lastGlyph) {
        int max = end - start;
        if (max == 0) return;
        boolean markupEnabled = this.markupEnabled;
        float scaleX = this.scaleX;
        ArrayList<BitmapCharacter> glyphs = run.glyphs;
        ArrayList<Float> xAdvances = run.xAdvances;

        // Guess at number of glyphs needed.
        glyphs.ensureCapacity(max);
        run.xAdvances.ensureCapacity(max + 1);

        do {
            char ch = str.charAt(start++);
            if (ch == '\r') continue; // Ignore.
            Glyph glyph = getGlyph(ch);
            if (glyph == null) {
                if (missingGlyph == null) continue;
                glyph = missingGlyph;
            }
            glyphs.add(glyph);
            xAdvances.add(lastGlyph == null // First glyph on line, adjust the position so it isn't drawn left of 0.
                    ? (glyph.isFixedWidth() ? 0 : -glyph.getXOffset() * scaleX - padLeft)
                    : (lastGlyph.getXAdvance() + lastGlyph.getKerning(ch)) * scaleX);
            lastGlyph = glyph;

            // "[[" is an escaped left square bracket, skip second character.
            if (markupEnabled && ch == '[' && start < end && str.charAt(start) == '[') start++;
        } while (start < end);
        if (lastGlyph != null) {
            float lastGlyphWidth = lastGlyph.isFixedWidth() ? lastGlyph.getXAdvance() * scaleX
                    : (lastGlyph.getWidth() + lastGlyph.getXOffset()) * scaleX - padRight;
            xAdvances.add(lastGlyphWidth);
        }
    }

    /** Returns the first valid glyph index to use to wrap to the next line, starting at the specified start index and
     * (typically) moving toward the beginning of the glyphs array. */
    public int getWrapIndex (List<BitmapCharacter> glyphs, int start) {
        int i = start - 1;
        Object[] glyphsItems = glyphs.toArray();
        char ch = ((BitmapCharacter)glyphsItems[i]).getChar();
        if (isWhitespace(ch)) return i;
        if (isBreakChar(ch)) i--;
        for (; i > 0; i--) {
            ch = ((BitmapCharacter)glyphsItems[i]).getChar();
            if (isWhitespace(ch) || isBreakChar(ch)) return i + 1;
        }
        return 0;
    }

    public boolean isBreakChar (char c) {
        if (breakChars == null) return false;
        for (char br : breakChars)
            if (c == br) return true;
        return false;
    }

    public boolean isWhitespace (char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    /** Returns the image path for the texture page at the given index (the "id" in the BMFont file). */
    public String getImagePath (int index) {
        return imagePaths[index];
    }

    public String[] getImagePaths () {
        return imagePaths;
    }

    public File getFontFile () {
        return fontFile;
    }

    /** Scales the font by the specified amounts on both axes
     * <p>
     * Note that smoother scaling can be achieved if the texture backing the BitmapFont is using {@link com.jme3.texture.Texture.MagFilter#Bilinear}.
     * The default is Nearest, so use a BitmapFont constructor that takes a {@link TextureRegion}.
     * @throws IllegalArgumentException if scaleX or scaleY is zero. */
    public void setScale (float scaleX, float scaleY) {
        if (scaleX == 0) throw new IllegalArgumentException("scaleX cannot be 0.");
        if (scaleY == 0) throw new IllegalArgumentException("scaleY cannot be 0.");
        float x = scaleX / this.scaleX;
        float y = scaleY / this.scaleY;
        lineHeight *= y;
        spaceXadvance *= x;
        xHeight *= y;
        capHeight *= y;
        ascent *= y;
        descent *= y;
        down *= y;
        padLeft *= x;
        padRight *= x;
        padTop *= y;
        padBottom *= y;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /** Scales the font by the specified amount in both directions.
     * @see #setScale(float, float)
     * @throws IllegalArgumentException if scaleX or scaleY is zero. */
    public void setScale (float scaleXY) {
        setScale(scaleXY, scaleXY);
    }

    /** Sets the font's scale relative to the current scale.
     * @see #setScale(float, float)
     * @throws IllegalArgumentException if the resulting scale is zero. */
    public void scale (float amount) {
        setScale(scaleX + amount, scaleY + amount);
    }

    public String toString () {
        return name != null ? name : super.toString();
    }
}
