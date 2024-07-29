package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This acts as a factory class for the 12 logical composite font
 * resources which are available as well as providing the implementation
 * of the resource.
 */
public class LogicalFont implements CompositeFontFile {

    public static final String SYSTEM     = "System";
    public static final String SERIF      = "Serif";
    public static final String SANS_SERIF = "SansSerif";
    public static final String MONOSPACED = "Monospaced";

    public static final String STYLE_REGULAR     = "Regular";
    public static final String STYLE_BOLD        = "Bold";
    public static final String STYLE_ITALIC      = "Italic";
    public static final String STYLE_BOLD_ITALIC = "Bold Italic";

    private static final Map<String, String> CANONICAL_FAMILY_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("system", SYSTEM);
        map.put("serif", SERIF);
        map.put("sansserif", SANS_SERIF);
        map.put("sans-serif", SANS_SERIF);// css style
        map.put("dialog", SANS_SERIF);
        map.put("default", SANS_SERIF);
        map.put("monospaced", MONOSPACED);
        map.put("monospace", MONOSPACED);
        map.put("dialoginput", MONOSPACED);// css style
        CANONICAL_FAMILY_MAP = Collections.unmodifiableMap(map);
    }
    static boolean isLogicalFont(String name) {
        int spaceIndex = name.indexOf(' ');
        if (spaceIndex != -1) {
            name = name.substring(0, spaceIndex);
        }
        return CANONICAL_FAMILY_MAP.get(name) != null;
    }

    private static String getCanonicalFamilyName(String name) {
         if (name == null) {
             return SANS_SERIF;
         }
         String lcName = name.toLowerCase();
         return CANONICAL_FAMILY_MAP.get(lcName);
    }

    static LogicalFont[] logicalFonts = new LogicalFont[16];

    static Font getLogicalFont(String familyName, boolean bold,
                               boolean italic, float size) {

        String canonicalFamilyName = getCanonicalFamilyName(familyName);
        if (canonicalFamilyName == null) {
            return null;
        }

        int fontIndex = 0;
        if (canonicalFamilyName.equals(SANS_SERIF)) {
            fontIndex = 0;
        } else if (canonicalFamilyName.equals(SERIF)) {
            fontIndex = 4;
       } else if (canonicalFamilyName.equals(MONOSPACED)) {
            fontIndex = 8;
        } else {
            fontIndex = 12;
        }
        if (bold) {
            fontIndex +=1;
        }
        if (italic) {
            fontIndex +=2;
        }

        LogicalFont font = logicalFonts[fontIndex];
        if (font == null) {
            font = new LogicalFont(canonicalFamilyName, bold, italic);
            logicalFonts[fontIndex] = font;
        }
        return new PrismFont(font, font.getFullName(), size);
    }

    static Font getLogicalFont(String fullName, float size) {

        /* Need to parse this to find the family portion, for which
         * we will allow the various spellings, and the style portion
         * which must be exactly one of those we understand. The matching
         * is however case insensitive.
         * Don't allow an absence of style, we want people to be
         * in the habit of distinguishing family and full name usage.
         * None of the family names we understand have a space, so look
         * for a space to delimit the family and style.
         */
        int spaceIndex = fullName.indexOf(' ');
        if (spaceIndex == -1 || spaceIndex == fullName.length()-1) {
            return null;
        }
        String family = fullName.substring(0, spaceIndex);
        String canonicalFamily = getCanonicalFamilyName(family);
        if (canonicalFamily == null) {
            return null;
        }
        String style = fullName.substring(spaceIndex+1).toLowerCase();
        boolean bold=false, italic=false;
        if (style.equals("regular")) {
            // nothing to do
        } else if (style.equals("bold")) {
            bold = true;
        } else if (style.equals("italic")) {
            italic = true;
        } else if (style.equals("bold italic")) {
            bold = true;
            italic = true;
        } else {
            return null;
        }
        return getLogicalFont(canonicalFamily, bold, italic, size);
    }

    boolean isBold, isItalic;
    private String fullName, familyName, styleName;
    private String physicalFamily;
    private String physicalFullName;
    private String physicalFileName;

    private LogicalFont(String family, boolean bold, boolean italic) {

        familyName = family;
        isBold = bold;
        isItalic = italic;

        if (!bold && !italic) {
            styleName = STYLE_REGULAR;
        } else if (bold && !italic) {
            styleName = STYLE_BOLD;
        } else if (!bold && italic) {
            styleName = STYLE_ITALIC;
        } else {
            styleName = STYLE_BOLD_ITALIC;
        }
        fullName = familyName + " " + styleName;
        if (PrismFontFactory.isLinux) {
//            FontConfigManager.FcCompFont fcCompFont =
//                FontConfigManager.getFontConfigFont(family, bold, italic);
//            physicalFullName = fcCompFont.firstFont.fullName;
//            physicalFileName = fcCompFont.firstFont.fontFile;
        } else {
            physicalFamily = PrismFontFactory.getSystemFont(familyName);
        }
    }

    private FontFile slot0FontFile;

    private FontFile getSlot0Resource() {
        if (slot0FontFile == null) {
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            if (physicalFamily != null) {
                slot0FontFile =  factory.getFontResource(physicalFamily,
                                                             isBold,
                                                             isItalic, false);
            } else {
                slot0FontFile = factory.getFontResource(physicalFullName,
                                                            physicalFileName,
                                                            false);
            }
            // Its unlikely but possible that this font isn't installed.
            if (slot0FontFile == null) {
                slot0FontFile = factory.getDefaultFontResource(false);
            }
        }
        return slot0FontFile;
    }

    volatile private String[] linkedFontNames;
    volatile private String[] linkedFontFiles;
    volatile private FontFile[] fallbacks;
    volatile private FontFile[] nativeFallbacks;

    private void getLinkedFonts() {
        if (fallbacks == null) {
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            FontFallbackInfo fallbackInfo = factory.getFallbacks(getSlot0Resource());
            linkedFontNames = fallbackInfo.getFontNames();
            linkedFontFiles = fallbackInfo.getFontFiles();
            fallbacks       = fallbackInfo.getFonts();
        }
    }

    @Override
    public int getNumSlots() {
        getLinkedFonts();
        int num = fallbacks.length;
        if (nativeFallbacks != null) {
            num += nativeFallbacks.length;
        }
        return num + 1;
    }

    private int getSlotForFontNoCreate(String fontName) {

        if (fontName.equals(getSlot0Resource().getFullName())) {
            return 0;
        }

        getLinkedFonts();
        int i = 1;
        for (String linkedFontName : linkedFontNames) {
            if (fontName.equalsIgnoreCase(linkedFontName)) {
                return i;
            }
            i++;
        }
        if (nativeFallbacks != null) {
            for (FontFile nativeFallback : nativeFallbacks) {
                if (fontName.equalsIgnoreCase(nativeFallback.getFullName())) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    @Override
    public int getSlotForFont(String fontName) {
        int slot = getSlotForFontNoCreate(fontName);
        if (slot >= 0) {
            return slot;
        }

        PrismFontFactory factory = PrismFontFactory.getFontFactory();
        FontFile fr = factory.getFontResource(fontName, null, false);
        if (fr == null) {
            if (PrismFontFactory.debugFonts) {
                System.err.println("\t Font name not supported \"" + fontName + "\".");
            }
            return -1;
        }
        slot = getSlotForFontNoCreate(fr.getFullName());
        if (slot >= 0) {
            return slot;
        }

        /* Add the font to the list of native fallbacks */
        return addNativeFallback(fr);
    }

    private int addNativeFallback(FontFile fr) {
        int ns = getNumSlots();
        if (ns >= 0x7E) {
            /* There are 8bits (0xFF) reserved in a glyph code to store the slot
             * number. The first bit cannot be set to avoid negative values
             * (leaving 0x7F). The extra -1 (leaving 0x7E) is to account for
             * the primary font resource in PrismCompositeFontResource.
             */
            if (PrismFontFactory.debugFonts) {
                System.err.println("\tToo many font fallbacks!");
            }
            return -1;
        }
        /* Add the font to the list of native fallbacks */
        FontFile[] tmp;
        if (nativeFallbacks == null) {
            tmp = new FontFile[1];
        } else {
            tmp = new FontFile[nativeFallbacks.length + 1];
            System.arraycopy(nativeFallbacks, 0, tmp, 0, nativeFallbacks.length);
        }
        tmp[tmp.length - 1] = fr;
        nativeFallbacks = tmp;

        return ns;
    }

    public int addSlotFont(FontFile fr) {
        if (fr == null) {
            return -1;
        }
        int slot = getSlotForFont(fr.getFullName());
        if (slot >= 0) {
            return slot;
        } else {
            return addNativeFallback(fr);
        }
    }

    @Override
    public FontFile getSlotResource(int slot) {
        if (slot == 0) {
            return getSlot0Resource();
        } else {
            getLinkedFonts();
            slot = slot - 1;
            if (slot >= fallbacks.length) {
                slot = slot - fallbacks.length;
                if (nativeFallbacks == null || slot >= nativeFallbacks.length) {
                    return null;
                }
                return nativeFallbacks[slot];
            }
            if (fallbacks[slot] == null) {
                String file = linkedFontFiles[slot];
                String name = linkedFontNames[slot];
                fallbacks[slot] =
                    PrismFontFactory.getFontFactory().
                          getFontResource(name, file, false);
                if (fallbacks[slot] == null) {
                    fallbacks[slot] = getSlot0Resource();
                }
            }
            return fallbacks[slot];
        }
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getPostscriptName() {
        return fullName;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public String getLocaleFullName() {
        return fullName;
    }

    @Override
    public String getLocaleFamilyName() {
        return familyName;
    }

    @Override
    public String getLocaleStyleName() {
        return styleName;
    }

    @Override
    public boolean isBold() {
        return getSlotResource(0).isBold();
    }

    @Override
    public boolean isItalic() {
        return getSlotResource(0).isItalic();
    }

    @Override
    public String getFileName() {
        return getSlotResource(0).getFileName();
    }

    @Override
    public int getFeatures() {
        return getSlotResource(0).getFeatures();
    }

    @Override
    public boolean isEmbeddedFont() {
        return getSlotResource(0).isEmbeddedFont();
    }

    @Override
    public float[] getGlyphBoundingBox(int glyphCode,
                                float size, float[] retArr) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        FontFile slotResource = getSlotResource(slot);
        return slotResource.getGlyphBoundingBox(slotglyphCode, size, retArr);
   }

    @Override
    public float getAdvance(int glyphCode, float size) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        FontFile slotResource = getSlotResource(slot);
        return slotResource.getAdvance(slotglyphCode, size);
    }

    CompositeGlyphMapper mapper;
    @Override
    public CharToGlyphMapper getGlyphMapper() {
        //return getSlot0Resource().getGlyphMapper();
        if (mapper == null) {
            mapper = new CompositeGlyphMapper(this);
        }
        return mapper;
    }

    Map<FontStrikeDesc, WeakReference<FontStrike>> strikeMap = new ConcurrentHashMap<>();

    @Override
    public Map<FontStrikeDesc, WeakReference<FontStrike>> getStrikeMap() {
        return strikeMap;
    }

    @Override
    public int getDefaultAAMode() {
        return getSlot0Resource().getDefaultAAMode();
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform) {
        return getStrike(size, transform, getDefaultAAMode());
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform,
                                int aaMode) {
        FontStrikeDesc desc= new FontStrikeDesc(size, transform, aaMode);
        WeakReference<FontStrike> ref = strikeMap.get(desc);
        CompositeStrike strike = null;

        if (ref != null) {
            strike = (CompositeStrike)ref.get();
        }
        if (strike == null) {
            strike = new CompositeStrike(this, size, transform, aaMode, desc);
            if (strike.disposer != null) {
                ref = Disposer.addRecord(strike, strike.disposer);
            } else {
                ref = new WeakReference<>(strike);
            }
            strikeMap.put(desc, ref);
        }
        return strike;
    }

    // Family 0 = SansSerif, 1 = Serif, 2 = Monospaced, 3 = System
    private static final int SANS_SERIF_INDEX = 0;
    private static final int SERIF_INDEX      = 1;
    private static final int MONOSPACED_INDEX = 2;
    private static final int SYSTEM_INDEX = 3;
    // Within a family styles are in the usual order
    static String[][] logFamilies = null;

    private static void buildFamily(String[] fullNames, String family) {
        fullNames[0] = family + " " + STYLE_REGULAR;
        fullNames[1] = family + " " + STYLE_BOLD;
        fullNames[2] = family + " " + STYLE_ITALIC;
        fullNames[3] = family + " " + STYLE_BOLD_ITALIC;
    }

    private static void buildFamilies() {
        if (logFamilies == null) {
            String[][] tmpFamilies = new String[SYSTEM_INDEX+1][4];
            buildFamily(tmpFamilies[SANS_SERIF_INDEX], SANS_SERIF);
            buildFamily(tmpFamilies[SERIF_INDEX], SERIF);
            buildFamily(tmpFamilies[MONOSPACED_INDEX], MONOSPACED);
            buildFamily(tmpFamilies[SYSTEM_INDEX], SYSTEM);
            logFamilies = tmpFamilies;
        }
    }

    static void addFamilies(ArrayList<String> familyList) {
        familyList.add(SANS_SERIF);
        familyList.add(SERIF);
        familyList.add(MONOSPACED);
        familyList.add(SYSTEM);
    }

    static void addFullNames(ArrayList<String> fullNames) {
        buildFamilies();
        for (int f = 0; f < logFamilies.length; f++) {
            for (int n = 0; n < logFamilies[f].length; n++) {
                fullNames.add(logFamilies[f][n]);
            }
        }
    }

    static String[] getFontsInFamily(String family) {
        String canonicalFamily = getCanonicalFamilyName(family);
        if (canonicalFamily == null) {
            return null;
        }
        buildFamilies();
        if (canonicalFamily.equals(SANS_SERIF)) {
            return logFamilies[SANS_SERIF_INDEX];
        } else if (canonicalFamily.equals(SERIF)) {
            return logFamilies[SERIF_INDEX];
        } else if (canonicalFamily.equals(MONOSPACED)) {
            return logFamilies[MONOSPACED_INDEX];
        } else {
            return logFamilies[SYSTEM_INDEX];
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LogicalFont)) {
            return false;
        }
        final LogicalFont other = (LogicalFont)obj;

        return this.fullName.equals(other.fullName);
    }

    private int hash;
    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        else {
            hash = fullName.hashCode();
            return hash;
        }
    }
}
