package io.github.jmecn.font.freetype;

import io.github.jmecn.font.exception.FtRuntimeException;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap_Size;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

import static io.github.jmecn.font.freetype.FtErrors.ok;
import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtFace implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(FtFace.class);

    private final FT_Face face;
    private FtGlyphSlot glyph;
    private FtSize size;
    private boolean isClosed;

    public FtFace(long address) {
        this.face = FT_Face.create(address);
        this.glyph = new FtGlyphSlot(face.glyph());
        this.isClosed = false;
    }

    public long address() {
        return face.address();
    }

    @Override
    public void close() {
        if (!isClosed) {
            FT_Done_Face(face);
            isClosed = true;
        }
    }

    public long getNumFaces() {
        return face.num_faces();
    }

    public long getFaceIndex() {
        return face.face_index();
    }

    public long getFaceFlags() {
        return face.face_flags();
    }

    public long getStyleFlags() {
        return face.style_flags();
    }

    public long getNumGlyphs() {
        return face.num_glyphs();
    }

    public String getFamilyName() {
        return face.family_nameString();
    }

    public String getStyleName() {
        return face.style_nameString();
    }

    public String getPostScriptName() {
        return FT_Get_Postscript_Name(face);
    }

    public String getFormat() {
        return FT_Get_Font_Format(face);
    }

    public int getNumFixedSizes() {
        return face.num_fixed_sizes();
    }

    public long getMaxAdvanceWidth() {
        return face.max_advance_width();
    }

    public long getMaxAdvanceHeight() {
        return face.max_advance_height();
    }

    /**
     * @param glyphIndex FtBitmapCharacter index of first character code. 0 if charmap is empty.
     * @return the first character code in the current charmap of a given face, together with its corresponding glyph index.
     */
    public long getFirstChar(IntBuffer glyphIndex) {
        return FT_Get_First_Char(face, glyphIndex);
    }

    /**
     * Return the next character code in the current charmap of a given face following the value char_code, as well as the corresponding glyph index.
     *
     * @param codepoint The starting character code.
     * @param glyphIndex FtBitmapCharacter index of next character code. 0 if charmap is empty.
     * @return The charmap's next character code.
     */
    public long getNextChar(long codepoint, IntBuffer glyphIndex) {
        return FT_Get_Next_Char(face, codepoint, glyphIndex);
    }

    public void selectCharmap(int encoding) {
        ok( FT_Select_Charmap(face, encoding) );
    }

    public int getCharmapIndex() {
        return FT_Get_Charmap_Index(face.charmap());
    }

    public void selectSize(int strikeIndex) {
        ok( FT_Select_Size(face, strikeIndex) );
    }

    public boolean setPixelSize(int pixelWidth, int pixelHeight) {
        try {
            ok(FT_Set_Pixel_Sizes(face, pixelWidth, pixelHeight));
            return true;
        } catch (FtRuntimeException e) {
            logger.error("Failed setPixelSize({}, {})", pixelWidth, pixelHeight, e);
            return false;
        }
    }

    public void setCharSize(int charWidth, int charHeight, int horzResolution, int vertResolution) {
        ok(FT_Set_Char_Size(face, charWidth, charHeight, horzResolution, vertResolution));
    }

    public int getCharIndex(int codepoint) {
        return FT_Get_Char_Index(face, codepoint);
    }

    public boolean loadGlyph(int glyphIndex, int loadFlags) {
        try {
            ok(FT_Load_Glyph(face, glyphIndex, loadFlags));
            return true;
        } catch (FtRuntimeException e) {
            logger.error("load glyph failed, glyphIndex:{}", glyphIndex, e);
            return false;
        }
    }

    public boolean loadChar(long codepoint) {
        return loadChar(codepoint, FT_LOAD_DEFAULT | FT_LOAD_FORCE_AUTOHINT);
    }

    public boolean loadChar(long codepoint, int loadFlags) {
        try {
            ok(FT_Load_Char(face, codepoint, loadFlags));
            return true;
        } catch (FtRuntimeException e) {
            logger.error("load char failed, codepoint:{}", codepoint, e);
            return false;
        }
    }

    public void setTransform(FT_Matrix matrix, FT_Vector delta) {
        FT_Set_Transform(face, matrix, delta);
    }

    public void getTransform(FT_Matrix matrix, FT_Vector delta) {
        FT_Get_Transform(face, matrix, delta);
    }

    public void renderGlyph(int renderMode) {
        ok( FT_Render_Glyph(face.glyph(), renderMode) );
    }

    public FtGlyphSlot getGlyph() {
        if (glyph == null) {
            glyph = new FtGlyphSlot(face.glyph());
        }
        return glyph;
    }

    public FtSize getSize() {
        if (size == null) {
            size = new FtSize(face.size());
        }
        return size;
    }

    public long getKerning(int leftGlyph, int rightGlyph, int kernMode) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FT_Vector kerning = FT_Vector.malloc(stack);
            ok(FT_Get_Kerning(face, leftGlyph, rightGlyph, kernMode, kerning));
            return kerning.x();
        } catch (FtRuntimeException e) {
            logger.error("Failed get kerning, leftGlyph:{}, rightGlyph:{}, kerningMode:{}", leftGlyph, rightGlyph, kernMode, e);
            return 0L;
        }
    }

    /////// Font testing Macros ///////
    private boolean testFlag(long flag) {
        return (face.face_flags() & flag) != 0L;
    }

    /**
     * @return true whenever a face object contains a scalable font face (true for TrueType, Type 1, Type 42, CID, OpenType/CFF, and PFR font formats).
     */
    public boolean isScalable() {
        return testFlag(FT_FACE_FLAG_SCALABLE);
    }

    /**
     * @return true whenever a face object contains some embedded bitmaps. See the available_sizes field of the FT_FaceRec structure.
     */
    public boolean hasFixedSizes() {
        return testFlag(FT_FACE_FLAG_FIXED_SIZES);
    }

    /**
     * @return true whenever a face object contains a font face that contains fixed-width (or ‘monospace’, ‘fixed-pitch’, etc.) glyphs.
     */
    public boolean isFixedWidth() {
        return testFlag(FT_FACE_FLAG_FIXED_WIDTH);
    }

    /**
     * @return true whenever a face object contains a font whose format is based on the SFNT storage scheme.
     * This usually means: TrueType fonts, OpenType fonts, as well as SFNT-based embedded bitmap fonts.
     */
    public boolean isSfnt() {
        return testFlag(FT_FACE_FLAG_SFNT);
    }

    /**
     * @return true whenever a face object contains horizontal metrics (this is true for all font formats though).
     */
    public boolean hasHorizontal() {
        return testFlag(FT_FACE_FLAG_HORIZONTAL);
    }

    /**
     * @return true whenever a face object contains real vertical metrics (and not only synthesized ones).
     */
    public boolean hasVertical() {
        return testFlag(FT_FACE_FLAG_VERTICAL);
    }

    /**
     * @return true whenever a face object contains kerning data that can be accessed with FT_Get_Kerning.
     */
    public boolean hasKerning() {
        return testFlag(FT_FACE_FLAG_KERNING);
    }

    /**
     * @return true whenever a face object contains some multiple masters.
     * The functions provided by FT_MULTIPLE_MASTERS_H are then available to choose the exact design you want.
     */
    public boolean hasMultipleMasters() {
        return testFlag(FT_FACE_FLAG_MULTIPLE_MASTERS);
    }

    /**
     * @return true whenever a face object contains some glyph names that can be accessed through FT_Get_Glyph_Name.
     * Note that some TrueType fonts contain broken glyph name tables. Use the function FT_Has_PS_Glyph_Names when needed.
     */
    public boolean hasGlyphNames() {
        return testFlag(FT_FACE_FLAG_GLYPH_NAMES);
    }

    /**
     * @return true whenever The font driver has a hinting machine of its own.
     * For example, with TrueType fonts, it makes sense to use data from the SFNT ‘gasp’ table only if the native
     * TrueType hinting engine (with the bytecode interpreter) is available and active.
     */
    public boolean hasHinter() {
        return testFlag(FT_FACE_FLAG_HINTER);
    }

    /**
     * <p>The face is CID-keyed. In that case, the face is not accessed by glyph indices but by CID values. For subsetted CID-keyed fonts this has the consequence that not all index values are a valid argument to FT_Load_Glyph. Only the CID values for which corresponding glyphs in the subsetted font exist make FT_Load_Glyph return successfully; in all other cases you get an FT_Err_Invalid_Argument error.</p>
     *
     * <p>Note that CID-keyed fonts that are in an SFNT wrapper (that is, all OpenType/CFF fonts) don't have this flag set since the glyphs are accessed in the normal way (using contiguous indices); the ‘CID-ness’ isn't visible to the application.</p>
     *
     * @return true whenever a face object contains a CID-keyed font. See the discussion of FT_FACE_FLAG_CID_KEYED for more details.
     */
    public boolean isCidKeyed() {
        return testFlag(FT_FACE_FLAG_CID_KEYED);
    }

    /**
     * <p>The face is ‘tricky’, that is, it always needs the font format's native hinting engine to get a reasonable result. A typical example is the old Chinese font mingli.ttf (but not mingliu.ttc) that uses TrueType bytecode instructions to move and scale all of its subglyphs.</p>
     *
     * <p>It is not possible to auto-hint such fonts using FT_LOAD_FORCE_AUTOHINT; it will also ignore FT_LOAD_NO_HINTING. You have to set both FT_LOAD_NO_HINTING and FT_LOAD_NO_AUTOHINT to really disable hinting; however, you probably never want this except for demonstration purposes.</p>
     *
     * <p>Currently, there are about a dozen TrueType fonts in the list of tricky fonts; they are hard-coded in file ttobjs.c.</p>
     *
     * @return true whenever a face represents a ‘tricky’ font. See the discussion of FT_FACE_FLAG_TRICKY for more details.
     */
    public boolean isTricky() {
        return testFlag(FT_FACE_FLAG_TRICKY);
    }

    /**
     * @return true whenever a face object contains tables for color glyphs.
     */
    public boolean hasColor() {
        return testFlag(FT_FACE_FLAG_COLOR);
    }

    /**
     * @return true whenever a face object has been altered by FT_Set_MM_Design_Coordinates, FT_Set_Var_Design_Coordinates, FT_Set_Var_Blend_Coordinates, or FT_Set_MM_WeightVector.
     */
    public boolean isVariation() {
        return testFlag(FT_FACE_FLAG_VARIATION);
    }

    /**
     * @return true whenever a face object contains an ‘SVG ’ OpenType table.
     */
    public boolean hasSvg() {
        return testFlag(FT_FACE_FLAG_SVG);
    }

    /**
     * @return true whenever a face object contains an ‘sbix’ OpenType table and outline glyphs.
     */
    public boolean hasSbix() {
        return testFlag(FT_FACE_FLAG_SBIX );
    }

    /**
     * @return true whenever a face object contains an ‘sbix’ OpenType table with bit 1 in its flags field set, instructing the application to overlay the bitmap strike with the corresponding outline glyph. See FT_HAS_SBIX for pseudo code how to use it.
     */
    public boolean hasSbixOverlay() {
        return testFlag(FT_FACE_FLAG_SBIX_OVERLAY);
    }

    /**
     * @return true whenever a face object is a named instance of a GX or OpenType variation font.
     */
    public boolean isNamedInstance() {
        return (face.face_index() & 0x7FFF0000L) != 0L;
    }

    //// for color font, eg: emoji ////
    public boolean hasEmoji() {
        int[] commonEmojiFont = new int[]{0x1F600, 0x1F64C, 0x1F64D, 0x1F64E, 0x1F64F, 0x1F680, 0x1F6C0, 0x1F6C1, 0x1F6C2, 0x1F6C3};
        for (int emoji : commonEmojiFont) {
            if (FT_Get_Char_Index(face, emoji) != 0) {
                return true;
            }
        }
        return false;
    }

    public void selectBestPixelSize(int pixelSize) {
        int count = face.num_fixed_sizes();
        if (count == 0) {
            logger.info("no fixed sizes");
            return;
        }

        int bestMatch = -1;
        int diff = Integer.MAX_VALUE;
        for (int i = 0; i < count; ++i) {
            FT_Bitmap_Size bitmapSize = face.available_sizes().get(i);

            logger.info("bitmapSize:{}, width:{}, height:{}, x_ppem:{}, y_ppem:{}", bitmapSize.size(), bitmapSize.width(), bitmapSize.height(), bitmapSize.x_ppem(), bitmapSize.y_ppem());

            int ndiff = Math.abs(pixelSize - bitmapSize.width());
            if (ndiff < diff) {
                bestMatch = i;
                diff = ndiff;
            }
        }
        ok( FT_Select_Size(face, bestMatch) );
    }

}
