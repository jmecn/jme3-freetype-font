package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface FontFile {

    /* Corresponds to FontSmoothingType enum values */
    int AA_GREYSCALE = 0;

    /* Font Features - not public API for now */
    // CSS naming
//    public static final int common_ligatures = 1 << 0;
//    public static final int discretionary_ligatures = 1 << 0;
//    public static final int historical_ligatures = 1 << 0;
//    public static final int contextual = 1 << 0;
//    public static final int small_caps = 1 << 0;
//    public static final int diagonal_fractions = 1 << 0;
//    public static final int stacked_fractions = 1 << 0;
//    public static final int slashed_zero = 1 << 0;

    // OpenType naming
    public static final int KERN = 1 << 0; // Kerning
    public static final int CLIG = 1 << 1; // Contextual Ligatures
    public static final int DLIG = 1 << 2; // Discretionary Ligatures
    public static final int HLIG = 1 << 3; // Historical Ligatures
    public static final int LIGA = 1 << 4; // Standard Ligatures
    public static final int RLIG = 1 << 5; // Required Liagtures
    public static final int LIGATURES = CLIG | DLIG | HLIG | LIGA | RLIG;
    public static final int SMCP = 1 << 6; // Small Capitals
    public static final int FRAC = 1 << 7; // Fractions
    public static final int AFRC = 1 << 8; // Alternative Fractions
    public static final int ZERO = 1 << 9; // Slashed Zero
    public static final int SWSH = 1 << 10; // Swash
    public static final int CSWH = 1 << 11; // Contextual Swash
    public static final int SALT = 1 << 12; // Stylistic Alternates
    public static final int NALT = 1 << 13; // Alternate Annotation Forms
    public static final int RUBY = 1 << 14; // Ruby Notation Forms
    public static final int SS01 = 1 << 15; // Stylistic Set 1
    public static final int SS02 = 1 << 16; // Stylistic Set 2
    public static final int SS03 = 1 << 17; // Stylistic Set 3
    public static final int SS04 = 1 << 18; // Stylistic Set 4
    public static final int SS05 = 1 << 19; // Stylistic Set 5
    public static final int SS06 = 1 << 20; // Stylistic Set 6
    public static final int SS07 = 1 << 21; // Stylistic Set 7
    //Note: the last two bits are reserved for layout. See GlyphLayout.

    String getFullName();

    String getPostscriptName();

    String getFamilyName();

    String getFileName();

    String getStyleName();

    String getLocaleFullName();

    String getLocaleFamilyName();

    String getLocaleStyleName();

    int getFeatures();

    boolean isBold();

    boolean isItalic();

    float getAdvance(int gc, float size);

    float[] getGlyphBoundingBox(int gc, float size, float[] retArr);

    int getDefaultAAMode();

    CharToGlyphMapper getGlyphMapper();

    Map<FontStrikeDesc, WeakReference<FontStrike>> getStrikeMap();

    FontStrike getStrike(float size, BaseTransform transform);
    FontStrike getStrike(float size, BaseTransform transform, int aaMode);
    boolean isEmbeddedFont();

    boolean isColorGlyph(int gc);
}
