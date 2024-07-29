package io.github.jmecn.font.sfntly;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.*;
import io.github.jmecn.font.*;
import io.github.jmecn.math.BaseTransform;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class SfntlyFontFile implements FontFile {
    /* MS locale id for US English is the "default" */
    public static final short MS_ENGLISH_LOCALE_ID = 0x0409; // 1033 decimal

    Font[] fonts;
    Font font;
    String familyName; /* Family font name (English) */
    String styleName;
    String fullName;   /* Full font name (English)   */
    String postscriptName; /* PostScript font name       */
    String localeFamilyName;
    String localeFullName;
    String localeStyleName;
    String filename;
    int numTables;
    int numGlyphs = -1;
    short indexToLocFormat;
    int fontIndex; // into a TTC.
    int fontCount; // in a TTC.
    boolean isCFF;
    boolean isEmbedded = false;
    boolean isCopy = false;
    boolean isTracked = false;
    boolean isRegistered = true;

    private boolean isBold;
    private boolean isItalic;
    private int fontWeight;
    private float upem;
    private float ascent;
    private float descent;
    private float linegap; // in design units
    private int numHMetrics;

    public SfntlyFontFile(String name, String filename, int fIndex, boolean register, boolean embedded,
                             boolean copy, boolean tracked) throws Exception {
        this.filename = filename;
        this.isRegistered = register;
        this.isEmbedded = embedded;
        this.isCopy = copy;
        this.isTracked = tracked;
        init(name, fIndex);
    }

    /**
     * Called from the constructor. Does the basic work of finding
     * the right font in a TTC, the font names and enough info
     * (the table offset directory) to be able to locate tables later.
     * Throws an exception if it doesn't like what it finds.
     */
    private void init(String name, int fIndex) throws Exception {

        FontFactory fontFactory = FontFactory.getInstance();
        this.fonts = fontFactory.loadFonts(new FileInputStream(filename));

        fontCount = fonts.length;
        try {
            if (fIndex >= fontCount) {
                throw new Exception("Bad collection index");
            }
            fontIndex = fIndex;

            this.font = fonts[fontIndex];

            // TODO check if this is a CFF font
            // isCFF = true;

            /* Now have the offset of this TT font (possibly within a TTC)
             * After the TT version/scaler type field, is the short
             * representing the number of tables in the table directory.
             * The table directory begins at 12 bytes after the header.
             * Each table entry is 16 bytes long (4 32-bit ints)
             */
            numTables = font.numTables();

            FontHeaderTable headTable = font.getTable(Tag.head);
            if (headTable == null) {
                throw new Exception("No head table - font is invalid." + filename);
            }
            upem = headTable.unitsPerEm();
            if (!(16 <= upem && upem <= 16384)) {
                upem = 2048;
            }

            indexToLocFormat = (short) headTable.indexToLocFormatAsInt();
            // 0 for short offsets, 1 for long
            if (indexToLocFormat < 0 || indexToLocFormat > 1) {
                throw new Exception("Bad indexToLocFormat");
            }

            // In a conventional optimised layout, the
            // hhea table immediately follows the 'head' table.
            HorizontalHeaderTable hhea = font.getTable(Tag.hhea);
            if (hhea == null) {
                numHMetrics = -1;
            } else {
                // the font table has the sign of ascent and descent
                // reversed from our coordinate system.
                ascent = hhea.ascender();
                descent = hhea.descender();
                linegap = hhea.lineGap();
                // advanceWidthMax is max horizontal advance of all glyphs in
                // font. For some fonts advanceWidthMax is much larger then "M"
                // advanceWidthMax = (float)hhea.getChar(10);
                numHMetrics = hhea.numberOfHMetrics();
                /* the hmtx table may have a trailing LSB array which we don't
                 * use. But it means we must not assume these two values match.
                 * We are only concerned here with not reading more data than
                 * there is in the table.
                 */
                HorizontalMetricsTable hmtx = font.getTable(Tag.hmtx);
                if (hmtx != null) {
                    int hmtxMetrics = hmtx.numberOfHMetrics();
                    if (numHMetrics > hmtxMetrics) {
                        numHMetrics = hmtxMetrics;
                    }
                }
            }

            // maxp table is before the OS/2 table. Read it now
            // while file is open - will be very cheap as its just
            // 32 bytes and we already have it in a byte[].
            MaximumProfileTable maxp = font.getTable(Tag.maxp);
            if (maxp != null) {
                numGlyphs = maxp.numGlyphs();
            }

            // setStyle
            // A number of fonts on Mac OS X do not have an OS/2 table.
            // For those need to get info from a different source.
            OS2Table os2Table = font.getTable(Tag.OS_2);
            if (os2Table != null) {
                int fsSelection = os2Table.fsSelectionAsInt();
                isItalic = (fsSelection & OS2Table.FsSelection.ITALIC.mask()) != 0;
                isBold = (fsSelection & OS2Table.FsSelection.BOLD.mask()) != 0;
                fontWeight = os2Table.usWeightClass();
            } else {
                int macStyleBits = headTable.macStyleAsInt();
                isItalic = (macStyleBits & FontHeaderTable.MacStyle.Italic.mask()) != 0;
                isBold = (macStyleBits & FontHeaderTable.MacStyle.Bold.mask()) != 0;
                fontWeight = 400;
            }

            /* Get names last, as the name table is far from the file header.
             * Although its also likely too big to fit in the read cache
             * in which case that would remain valid, but also will help
             * any file read implementation which doesn't have random access.
             */
            initNames();

            if (familyName == null || fullName == null) {
                String fontName = name != null ? name : "";
                if (fullName == null) {
                    fullName = familyName != null ? familyName : fontName;
                }
                if (familyName == null) {
                    familyName = fullName;
                }
                throw new Exception("Font name not found in " + filename);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    void initNames() {
        NameTable nameTable = font.getTable(Tag.name);

        /* Microsoft Windows font names are preferred but numerous Mac
         * fonts do not have these, so we must also accept these in the
         * absence of the preferred Windows names.
         */
        int windowsEnglishLocaleId = NameTable.WindowsLanguageId.English_UnitedStates.value();// 1033
        for (NameTable.NameEntry nameEntry : nameTable) {
            int platformID = nameEntry.platformId();
            if ((platformID != Font.PlatformId.Unicode.value()) &&
                    (platformID != Font.PlatformId.Windows.value()) &&
                    (platformID != Font.PlatformId.Macintosh.value())) {
                continue; // skip over this record.
            }
            int encodingID = nameEntry.encodingId();
            // only want UTF-16 (inc. symbol) encodingIDs for Windows or MacRoman on Mac.
            if ((platformID == Font.PlatformId.Windows.value() && encodingID > 1) ||
                    (platformID == Font.PlatformId.Macintosh.value() &&
                            encodingID != Font.MacintoshEncodingId.Roman.value())) {
                continue;
            }
            int langID = nameEntry.languageId();
            if (platformID == Font.PlatformId.Macintosh.value() &&
                    langID != NameTable.MacintoshLanguageId.English.value()) {
                continue;
            }
            int nameID   = nameEntry.nameId();
            String tmpName = nameEntry.name();

            if (nameID == NameTable.NameId.FontFamilyName.value()) {
                // Family
                if (familyName == null || langID == MS_ENGLISH_LOCALE_ID || langID == nameLocaleID) {
                    if (familyName == null || langID == MS_ENGLISH_LOCALE_ID){
                        familyName = tmpName;
                    }
                    if (langID == nameLocaleID) {
                        localeFamilyName = tmpName;
                    }
                }
            } else if (nameID == NameTable.NameId.FontSubfamilyName.value()) {
                // Style
                if (styleName == null || langID == MS_ENGLISH_LOCALE_ID || langID == nameLocaleID) {
                    if (styleName == null || langID == MS_ENGLISH_LOCALE_ID) {
                        styleName = tmpName;
                    }
                    if (langID == nameLocaleID) {
                        localeStyleName = tmpName;
                    }
                }
            } else if (nameID == NameTable.NameId.FullFontName.value()) {
                if (fullName == null || langID == MS_ENGLISH_LOCALE_ID || langID == nameLocaleID) {
                    if (fullName == null || langID == MS_ENGLISH_LOCALE_ID) {
                        fullName = tmpName;
                    }
                    if (langID == nameLocaleID) {
                        localeFullName = tmpName;
                    }
                }
            } else if (nameID == NameTable.NameId.PostscriptName.value()) {
                if (postscriptName == null) {
                    postscriptName = tmpName;
                }
            }

            if (localeFamilyName == null) {
                localeFamilyName = familyName;
            }
            if (localeFullName == null) {
                localeFullName = fullName;
            }
            if (localeStyleName == null) {
                localeStyleName = styleName;
            }
        }
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getPostscriptName() {
        return postscriptName;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public String getLocaleFullName() {
        return localeFullName;
    }

    @Override
    public String getLocaleFamilyName() {
        return localeFamilyName;
    }

    @Override
    public String getLocaleStyleName() {
        return localeStyleName;
    }

    @Override
    public int getFeatures() {
        return 0;
    }

    @Override
    public boolean isBold() {
        return isBold;
    }

    @Override
    public boolean isItalic() {
        return isItalic;
    }

    @Override
    public float getAdvance(int gc, float size) {
        return 0;
    }

    @Override
    public float[] getGlyphBoundingBox(int gc, float size, float[] retArr) {
        return new float[0];
    }

    @Override
    public int getDefaultAAMode() {
        return 0;
    }

    @Override
    public CharToGlyphMapper getGlyphMapper() {
        return null;
    }

    @Override
    public Map<FontStrikeDesc, WeakReference<FontStrike>> getStrikeMap() {
        return null;
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform) {
        return null;
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform, int aaMode) {
        return null;
    }

    @Override
    public boolean isEmbeddedFont() {
        return false;
    }

    @Override
    public boolean isColorGlyph(int gc) {
        return false;
    }

    int getNumGlyphs() {
        return numGlyphs;
    }

    /*** BEGIN LOCALE_ID MAPPING ****/

    private static Map<String, Short> lcidMap;

    // Return a Microsoft LCID from the given Locale.
    // Used when getting localized font data.

    private static void addLCIDMapEntry(Map<String, Short> map,
                                        String key, short value) {
        map.put(key, Short.valueOf(value));
    }

    private static synchronized void createLCIDMap() {
        if (lcidMap != null) {
            return;
        }

        Map<String, Short> map = new HashMap<>(200);
        addLCIDMapEntry(map, "ar", (short) 0x0401);
        addLCIDMapEntry(map, "bg", (short) 0x0402);
        addLCIDMapEntry(map, "ca", (short) 0x0403);
        addLCIDMapEntry(map, "zh", (short) 0x0404);
        addLCIDMapEntry(map, "cs", (short) 0x0405);
        addLCIDMapEntry(map, "da", (short) 0x0406);
        addLCIDMapEntry(map, "de", (short) 0x0407);
        addLCIDMapEntry(map, "el", (short) 0x0408);
        addLCIDMapEntry(map, "en", (short) 0x0409);
        addLCIDMapEntry(map, "en_US", (short) 0x0409);
        addLCIDMapEntry(map, "es", (short) 0x040a);
        addLCIDMapEntry(map, "fi", (short) 0x040b);
        addLCIDMapEntry(map, "fr", (short) 0x040c);
        addLCIDMapEntry(map, "iw", (short) 0x040d);
        addLCIDMapEntry(map, "hu", (short) 0x040e);
        addLCIDMapEntry(map, "is", (short) 0x040f);
        addLCIDMapEntry(map, "it", (short) 0x0410);
        addLCIDMapEntry(map, "ja", (short) 0x0411);
        addLCIDMapEntry(map, "ko", (short) 0x0412);
        addLCIDMapEntry(map, "nl", (short) 0x0413);
        addLCIDMapEntry(map, "no", (short) 0x0414);
        addLCIDMapEntry(map, "pl", (short) 0x0415);
        addLCIDMapEntry(map, "pt", (short) 0x0416);
        addLCIDMapEntry(map, "rm", (short) 0x0417);
        addLCIDMapEntry(map, "ro", (short) 0x0418);
        addLCIDMapEntry(map, "ru", (short) 0x0419);
        addLCIDMapEntry(map, "hr", (short) 0x041a);
        addLCIDMapEntry(map, "sk", (short) 0x041b);
        addLCIDMapEntry(map, "sq", (short) 0x041c);
        addLCIDMapEntry(map, "sv", (short) 0x041d);
        addLCIDMapEntry(map, "th", (short) 0x041e);
        addLCIDMapEntry(map, "tr", (short) 0x041f);
        addLCIDMapEntry(map, "ur", (short) 0x0420);
        addLCIDMapEntry(map, "in", (short) 0x0421);
        addLCIDMapEntry(map, "uk", (short) 0x0422);
        addLCIDMapEntry(map, "be", (short) 0x0423);
        addLCIDMapEntry(map, "sl", (short) 0x0424);
        addLCIDMapEntry(map, "et", (short) 0x0425);
        addLCIDMapEntry(map, "lv", (short) 0x0426);
        addLCIDMapEntry(map, "lt", (short) 0x0427);
        addLCIDMapEntry(map, "fa", (short) 0x0429);
        addLCIDMapEntry(map, "vi", (short) 0x042a);
        addLCIDMapEntry(map, "hy", (short) 0x042b);
        addLCIDMapEntry(map, "eu", (short) 0x042d);
        addLCIDMapEntry(map, "mk", (short) 0x042f);
        addLCIDMapEntry(map, "tn", (short) 0x0432);
        addLCIDMapEntry(map, "xh", (short) 0x0434);
        addLCIDMapEntry(map, "zu", (short) 0x0435);
        addLCIDMapEntry(map, "af", (short) 0x0436);
        addLCIDMapEntry(map, "ka", (short) 0x0437);
        addLCIDMapEntry(map, "fo", (short) 0x0438);
        addLCIDMapEntry(map, "hi", (short) 0x0439);
        addLCIDMapEntry(map, "mt", (short) 0x043a);
        addLCIDMapEntry(map, "se", (short) 0x043b);
        addLCIDMapEntry(map, "gd", (short) 0x043c);
        addLCIDMapEntry(map, "ms", (short) 0x043e);
        addLCIDMapEntry(map, "kk", (short) 0x043f);
        addLCIDMapEntry(map, "ky", (short) 0x0440);
        addLCIDMapEntry(map, "sw", (short) 0x0441);
        addLCIDMapEntry(map, "tt", (short) 0x0444);
        addLCIDMapEntry(map, "bn", (short) 0x0445);
        addLCIDMapEntry(map, "pa", (short) 0x0446);
        addLCIDMapEntry(map, "gu", (short) 0x0447);
        addLCIDMapEntry(map, "ta", (short) 0x0449);
        addLCIDMapEntry(map, "te", (short) 0x044a);
        addLCIDMapEntry(map, "kn", (short) 0x044b);
        addLCIDMapEntry(map, "ml", (short) 0x044c);
        addLCIDMapEntry(map, "mr", (short) 0x044e);
        addLCIDMapEntry(map, "sa", (short) 0x044f);
        addLCIDMapEntry(map, "mn", (short) 0x0450);
        addLCIDMapEntry(map, "cy", (short) 0x0452);
        addLCIDMapEntry(map, "gl", (short) 0x0456);
        addLCIDMapEntry(map, "dv", (short) 0x0465);
        addLCIDMapEntry(map, "qu", (short) 0x046b);
        addLCIDMapEntry(map, "mi", (short) 0x0481);
        addLCIDMapEntry(map, "ar_IQ", (short) 0x0801);
        addLCIDMapEntry(map, "zh_CN", (short) 0x0804);
        addLCIDMapEntry(map, "de_CH", (short) 0x0807);
        addLCIDMapEntry(map, "en_GB", (short) 0x0809);
        addLCIDMapEntry(map, "es_MX", (short) 0x080a);
        addLCIDMapEntry(map, "fr_BE", (short) 0x080c);
        addLCIDMapEntry(map, "it_CH", (short) 0x0810);
        addLCIDMapEntry(map, "nl_BE", (short) 0x0813);
        addLCIDMapEntry(map, "no_NO_NY", (short) 0x0814);
        addLCIDMapEntry(map, "pt_PT", (short) 0x0816);
        addLCIDMapEntry(map, "ro_MD", (short) 0x0818);
        addLCIDMapEntry(map, "ru_MD", (short) 0x0819);
        addLCIDMapEntry(map, "sr_CS", (short) 0x081a);
        addLCIDMapEntry(map, "sv_FI", (short) 0x081d);
        addLCIDMapEntry(map, "az_AZ", (short) 0x082c);
        addLCIDMapEntry(map, "se_SE", (short) 0x083b);
        addLCIDMapEntry(map, "ga_IE", (short) 0x083c);
        addLCIDMapEntry(map, "ms_BN", (short) 0x083e);
        addLCIDMapEntry(map, "uz_UZ", (short) 0x0843);
        addLCIDMapEntry(map, "qu_EC", (short) 0x086b);
        addLCIDMapEntry(map, "ar_EG", (short) 0x0c01);
        addLCIDMapEntry(map, "zh_HK", (short) 0x0c04);
        addLCIDMapEntry(map, "de_AT", (short) 0x0c07);
        addLCIDMapEntry(map, "en_AU", (short) 0x0c09);
        addLCIDMapEntry(map, "fr_CA", (short) 0x0c0c);
        addLCIDMapEntry(map, "sr_CS", (short) 0x0c1a);
        addLCIDMapEntry(map, "se_FI", (short) 0x0c3b);
        addLCIDMapEntry(map, "qu_PE", (short) 0x0c6b);
        addLCIDMapEntry(map, "ar_LY", (short) 0x1001);
        addLCIDMapEntry(map, "zh_SG", (short) 0x1004);
        addLCIDMapEntry(map, "de_LU", (short) 0x1007);
        addLCIDMapEntry(map, "en_CA", (short) 0x1009);
        addLCIDMapEntry(map, "es_GT", (short) 0x100a);
        addLCIDMapEntry(map, "fr_CH", (short) 0x100c);
        addLCIDMapEntry(map, "hr_BA", (short) 0x101a);
        addLCIDMapEntry(map, "ar_DZ", (short) 0x1401);
        addLCIDMapEntry(map, "zh_MO", (short) 0x1404);
        addLCIDMapEntry(map, "de_LI", (short) 0x1407);
        addLCIDMapEntry(map, "en_NZ", (short) 0x1409);
        addLCIDMapEntry(map, "es_CR", (short) 0x140a);
        addLCIDMapEntry(map, "fr_LU", (short) 0x140c);
        addLCIDMapEntry(map, "bs_BA", (short) 0x141a);
        addLCIDMapEntry(map, "ar_MA", (short) 0x1801);
        addLCIDMapEntry(map, "en_IE", (short) 0x1809);
        addLCIDMapEntry(map, "es_PA", (short) 0x180a);
        addLCIDMapEntry(map, "fr_MC", (short) 0x180c);
        addLCIDMapEntry(map, "sr_BA", (short) 0x181a);
        addLCIDMapEntry(map, "ar_TN", (short) 0x1c01);
        addLCIDMapEntry(map, "en_ZA", (short) 0x1c09);
        addLCIDMapEntry(map, "es_DO", (short) 0x1c0a);
        addLCIDMapEntry(map, "sr_BA", (short) 0x1c1a);
        addLCIDMapEntry(map, "ar_OM", (short) 0x2001);
        addLCIDMapEntry(map, "en_JM", (short) 0x2009);
        addLCIDMapEntry(map, "es_VE", (short) 0x200a);
        addLCIDMapEntry(map, "ar_YE", (short) 0x2401);
        addLCIDMapEntry(map, "es_CO", (short) 0x240a);
        addLCIDMapEntry(map, "ar_SY", (short) 0x2801);
        addLCIDMapEntry(map, "en_BZ", (short) 0x2809);
        addLCIDMapEntry(map, "es_PE", (short) 0x280a);
        addLCIDMapEntry(map, "ar_JO", (short) 0x2c01);
        addLCIDMapEntry(map, "en_TT", (short) 0x2c09);
        addLCIDMapEntry(map, "es_AR", (short) 0x2c0a);
        addLCIDMapEntry(map, "ar_LB", (short) 0x3001);
        addLCIDMapEntry(map, "en_ZW", (short) 0x3009);
        addLCIDMapEntry(map, "es_EC", (short) 0x300a);
        addLCIDMapEntry(map, "ar_KW", (short) 0x3401);
        addLCIDMapEntry(map, "en_PH", (short) 0x3409);
        addLCIDMapEntry(map, "es_CL", (short) 0x340a);
        addLCIDMapEntry(map, "ar_AE", (short) 0x3801);
        addLCIDMapEntry(map, "es_UY", (short) 0x380a);
        addLCIDMapEntry(map, "ar_BH", (short) 0x3c01);
        addLCIDMapEntry(map, "es_PY", (short) 0x3c0a);
        addLCIDMapEntry(map, "ar_QA", (short) 0x4001);
        addLCIDMapEntry(map, "es_BO", (short) 0x400a);
        addLCIDMapEntry(map, "es_SV", (short) 0x440a);
        addLCIDMapEntry(map, "es_HN", (short) 0x480a);
        addLCIDMapEntry(map, "es_NI", (short) 0x4c0a);
        addLCIDMapEntry(map, "es_PR", (short) 0x500a);

        lcidMap = map;
    }

    private static short getLCIDFromLocale(Locale locale) {
        // optimize for common case
        if (locale.equals(Locale.US) || locale.getLanguage().equals("en")) {
            return MS_ENGLISH_LOCALE_ID;// 1033
        }

        if (lcidMap == null) {
            createLCIDMap();
        }

        String key = locale.toString();
        while (!key.isEmpty()) {
            Short lcidObject = lcidMap.get(key);
            if (lcidObject != null) {
                return lcidObject;
            }
            int pos = key.lastIndexOf('_');
            if (pos < 1) {
                return MS_ENGLISH_LOCALE_ID;
            }
            key = key.substring(0, pos);
        }

        return MS_ENGLISH_LOCALE_ID;
    }


    /* On Windows this is set to the System Locale, which matches how
     * GDI enumerates font names. For display purposes we may want
     * the user locale which could be different.
     */
    static short nameLocaleID = getSystemLCID();

    private static short getSystemLCID() {
        return getLCIDFromLocale(Locale.getDefault());
    }

    @Override
    public String toString() {
        return "SfntlyFontFile{" +
                "familyName='" + familyName + '\'' +
                ", styleName='" + styleName + '\'' +
                ", psName='" + postscriptName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", localeFamilyName='" + localeFamilyName + '\'' +
                ", localeStyleName='" + localeStyleName + '\'' +
                ", localeFullName='" + localeFullName + '\'' +
                ", filename='" + filename + '\'' +
                ", fontIndex=" + fontIndex +
                ", fontCount=" + fontCount +
                ", numTables=" + numTables +
                ", numGlyphs=" + numGlyphs +
                ", indexToLocFormat=" + indexToLocFormat +
                ", isCFF=" + isCFF +
                ", isBold=" + isBold +
                ", isItalic=" + isItalic +
                ", fontWeight=" + fontWeight +
                ", upem=" + upem +
                ", ascent=" + ascent +
                ", descent=" + descent +
                ", linegap=" + linegap +
                ", numHMetrics=" + numHMetrics +
                '}';
    }
}
