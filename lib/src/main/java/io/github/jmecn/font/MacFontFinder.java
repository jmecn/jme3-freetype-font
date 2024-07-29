package io.github.jmecn.font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MacFontFinder {

    private static final int SystemFontType = 2; /*kCTFontSystemFontType*/
    private static final int MonospacedFontType = 1; /*kCTFontUserFixedPitchFontType*/

    private static native String getFont(int type);
    public static String getSystemFont() {
        return getFont(SystemFontType);
    }

    public static String getMonospacedFont() {
        return getFont(MonospacedFontType);
    }

    static native float getSystemFontSize();

    public static boolean populateFontFileNameMap(
            HashMap<String,String> fontToFileMap,
            HashMap<String,String> fontToFamilyNameMap,
            HashMap<String,ArrayList<String>> familyToFontListMap,
            Locale locale) {

        if (fontToFileMap == null ||
            fontToFamilyNameMap == null ||
            familyToFontListMap == null) {
            return false;
        }
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        String[] fontData = getFontData();
        if (fontData == null) {
            return false;
        }

        int i = 0;
        while (i < fontData.length) {
            String name = fontData[i++];
            String family = fontData[i++];
            String file = fontData[i++];

            if (name == null || family == null || file == null) {
                continue;
            }
            String lcName = name.toLowerCase(locale);
            String lcFamily = family.toLowerCase(locale);
            fontToFileMap.put(lcName, file);
            fontToFamilyNameMap.put(lcName, family);
            ArrayList<String> list = familyToFontListMap.get(lcFamily);
            if (list == null) {
                list = new ArrayList<>();
                familyToFontListMap.put(lcFamily, list);
            }
            list.add(name);
        }
        return true;
    }
    /*
     *
     * @param familyName
     * @return array of post-script font names
     */
    private native static String[] getFontData();

    public native static String[] getCascadeList(long fontRef);
    public native static long[] getCascadeListRefs(long fontRef);
}