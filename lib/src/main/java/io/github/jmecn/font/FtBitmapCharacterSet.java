package io.github.jmecn.font;

import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.material.Material;
import com.jme3.texture.Image;
import com.jme3.util.IntMap;
import io.github.jmecn.font.freetype.FtFace;
import io.github.jmecn.font.freetype.FtLibrary;
import io.github.jmecn.font.freetype.FtStroker;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.FT_KERNING_DEFAULT;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtBitmapCharacterSet extends BitmapCharacterSet implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(FtBitmapCharacterSet.class);

    /** The name of the font, or null. */
    public String name;

    // hold the materials
    private final IntMap<Material> materials;

    public float padTop;
    public float padRight;
    public float padBottom;
    public float padLeft;
    /** The distance from one line of text to the next. To set this value, use {@link #setLineHeight(int)}. */
    public int lineHeight;
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
    public float scaleX = 1;
    public float scaleY = 1;
    public boolean markupEnabled;
    /** The amount to add to the glyph X position when drawing a cursor between glyphs. This field is not set by the BMFont
     * file, it needs to be set manually depending on how the glyphs are rendered on the backing textures. */
    public float cursorX;

    public final IntMap<IntMap<Glyph>> characters;
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

    ///////////////


    List<Image> images;

    // Fields for incremental glyph generation.
    FtFontGenerator generator;
    FtFontParameter parameter;
    FtStroker stroker;
    Packer packer;
    List<Glyph> glyphs;
    private boolean dirty;

    public FtBitmapCharacterSet() {
        images = new ArrayList<>();
        characters = new IntMap<>();
        materials = new IntMap<>();
        glyphs = new ArrayList<>(128);// all ascii chars
    }

    public void addCharacter(int ch, Glyph glyph) {
        getCharacterSet(0).put(ch, glyph);
    }
    public void addCharacter(int style, int ch, Glyph glyph) {
        getCharacterSet(style).put(ch, glyph);
    }

    public Glyph getFirstGlyph () {
        if (glyphs.isEmpty()) {
            return null;
        }
        return glyphs.get(0);
    }

    /** Returns true if the font has the glyph, or if the font has a {@link #missingGlyph}. */
    public boolean hasGlyph(char ch) {
        if (missingGlyph != null) {
            return true;
        }
        return getCharacter(ch) != null;
    }

    private IntMap<Glyph> getCharacterSet(int style) {
        if (characters.containsKey(style)) {
            return characters.get(style);
        } else {
            IntMap<Glyph> map = new IntMap<>();
            characters.put(style, map);
            return map;
        }
    }

    @Override
    public Glyph getCharacter(int ch) {
        return getCharacter(ch, 0);
    }

    @Override
    public Glyph getCharacter(int ch, int style) {
        // get cached character
        IntMap<Glyph> charset = getCharacterSet(style);
        Glyph glyph = charset.get(ch);

        if (glyph == null && generator != null) {
            generator.setPixelSizes(0, parameter.getSize());
            float baseline = (ascent + capHeight) / scaleY;
            glyph = generator.createGlyph((char) ch, parameter, stroker, baseline, packer);
            if (glyph == null) {
                return missingGlyph;
            }

            charset.put(ch, glyph);
            glyphs.add(glyph);
            dirty = true;

            FtFace face = generator.getFace();
            if (parameter.isKerning()) {
                int glyphIndex = face.getCharIndex(ch);
                for (int i = 0, n = glyphs.size(); i < n; i++) {
                    Glyph other = glyphs.get(i);
                    int otherIndex = face.getCharIndex(other.getChar());

                    long kerning = face.getKerning(glyphIndex, otherIndex, FT_KERNING_DEFAULT);
                    if (kerning != 0) glyph.addKerning(other.getChar(), FtLibrary.from26D6(kerning));

                    kerning = face.getKerning(otherIndex, glyphIndex, FT_KERNING_DEFAULT);
                    if (kerning != 0) other.addKerning(ch, FtLibrary.from26D6(kerning));
                }
            }
        }
        return glyph;
    }

    public List<Glyph> getGlyphs() {
        // FIXME remove this method after change the implementation of FtBitmapCharacterSet to BitmapCharacterSet
        return glyphs;
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

    /** Scales the font by the specified amounts on both axes
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

    ///// getters & setters /////

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    /** Sets the line height, which is the distance from one line of text to the next. */
    @Override
    public void setLineHeight(int lineHeight) {
        this.lineHeight = (int) (lineHeight * scaleY);
        down = - this.lineHeight;
    }

    public float getPadTop() {
        return padTop;
    }

    public void setPadTop(float padTop) {
        this.padTop = padTop;
    }

    public float getPadRight() {
        return padRight;
    }

    public void setPadRight(float padRight) {
        this.padRight = padRight;
    }

    public float getPadBottom() {
        return padBottom;
    }

    public void setPadBottom(float padBottom) {
        this.padBottom = padBottom;
    }

    public float getPadLeft() {
        return padLeft;
    }

    public void setPadLeft(float padLeft) {
        this.padLeft = padLeft;
    }

    public float getCapHeight() {
        return capHeight;
    }

    public void setCapHeight(float capHeight) {
        this.capHeight = capHeight;
    }

    public float getAscent() {
        return ascent;
    }

    public void setAscent(float ascent) {
        this.ascent = ascent;
    }

    public float getDescent() {
        return descent;
    }

    public void setDescent(float descent) {
        this.descent = descent;
    }

    public float getDown() {
        return down;
    }

    public void setDown(float down) {
        this.down = down;
    }

    public float getBlankLineScale() {
        return blankLineScale;
    }

    public void setBlankLineScale(float blankLineScale) {
        this.blankLineScale = blankLineScale;
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

    public boolean isMarkupEnabled() {
        return markupEnabled;
    }

    public void setMarkupEnabled(boolean markupEnabled) {
        this.markupEnabled = markupEnabled;
    }

    public float getSpaceXadvance() {
        return spaceXadvance;
    }

    public void setSpaceXadvance(float spaceXadvance) {
        this.spaceXadvance = spaceXadvance;
    }

    public float getxHeight() {
        return xHeight;
    }

    public void setxHeight(float xHeight) {
        this.xHeight = xHeight;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setGenerator(FtFontGenerator generator) {
        this.generator = generator;
    }

    public void setParameter(FtFontParameter parameter) {
        this.parameter = parameter;
    }

    public void setStroker(FtStroker stroker) {
        this.stroker = stroker;
    }

    public void setPacker(Packer packer) {
        this.packer = packer;
    }
//////////

    public String toString () {
        return name != null ? name : super.toString();
    }

    @Override
    public void close() {
        if (stroker != null) stroker.close();
        if (packer != null) packer.close();
    }

    public void addMaterial(int page, Material material) {
        materials.put(page, material);
    }

    public Material getMaterial(int page) {
        return materials.get(page);
    }

    public int getPageSize() {
        return materials.size();
    }

    public void addImage(Image image) {
        images.add(image);
    }

    public Image getImage(int page) {
        return images.get(page);
    }

    public List<Image> getImages() {
        return images;
    }
}
