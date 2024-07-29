package io.github.jmecn.font;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import io.github.jmecn.text.GlyphLayout;

public abstract class PrismFontFactory implements FontFactory {

    public static final boolean debugFonts;
    public static final boolean isWindows;
    public static final boolean isLinux;
    public static final boolean isMacOSX;
    public static final boolean isIOS;
    public static final boolean isAndroid;
    public static final int cacheLayoutSize;
    private static float fontSizeLimit = 80f;

    /* We need two maps. One to hold pointers to the raw fonts, another
     * to hold pointers to the composite resources. Top level look ups
     * to createFont() will look first in the compResourceMap, and
     * only go to the second map to create a wrapped resource.
     * Logical Fonts are handled separately.
     */
    HashMap<String, FontFile> fontResourceMap = new HashMap<>();

    HashMap<String, CompositeFontFile> compResourceMap = new HashMap<>();

    static {
        Platform platform = JmeSystem.getPlatform();
        isWindows = platform.getOs() == Platform.Os.Windows;
        isMacOSX  = platform.getOs() == Platform.Os.MacOS;
        isLinux   = platform.getOs() == Platform.Os.Linux;
        isIOS     = platform.getOs() == Platform.Os.iOS;
        isAndroid = platform.getOs() == Platform.Os.Android;
        int[] tempCacheLayoutSize = {0x10000};
        systemFontSize = -1f;
        debugFonts = true;
        cacheLayoutSize = tempCacheLayoutSize[0];
    }

    public static float getFontSizeLimit() {
        return fontSizeLimit;
    }

    private static PrismFontFactory theFontFactory = null;
    public static synchronized PrismFontFactory getFontFactory() {
        if (theFontFactory != null) {
            return theFontFactory;
        }
        theFontFactory = null;// TODO
        return theFontFactory;
    }

    private HashMap<String, FontFileImpl> fileNameToFontResourceMap = new HashMap<>();

    protected abstract FontFileImpl
          createFontFile(String name, String filename,
                         int fIndex, boolean register,
                         boolean embedded,
                         boolean copy, boolean tracked)
                         throws Exception;

    public abstract GlyphLayout createGlyphLayout();

    // For an caller who has recognised a TTC file and wants to create
    // the instances one at a time so as to have visibility into the
    // contents of the TTC. Onus is on caller to enumerate all the fonts.
    private FontFileImpl createFontResource(String filename, int index) {
        return createFontResource(null, filename, index,
                                  true, false, false, false);
    }

    private FontFileImpl createFontResource(String name,
                                            String filename, int index,
                                            boolean register, boolean embedded,
                                            boolean copy, boolean tracked) {
        String key = (filename+index).toLowerCase();
        /*
         * macOS: we need to load unique fonts for regular and bold.
         * Probably this should be handled elsewhere
         */
        if (isMacOSX && (name != null) && name.startsWith("System ")) {
            key += name;
        }
        FontFileImpl fr = fileNameToFontResourceMap.get(key);
        if (fr != null) {
            return fr;
        }

        try {
            fr = createFontFile(name, filename, index, register,
                                embedded, copy, tracked);
            if (register) {
                // Although it looks tempting here to store the lookup name
                // as what we matched, it isn't safe to do so.
                // We may be iterating over fonts in a TTC, or (some day)
                // a TTF with font variations. So we don't want to just store the name
                // But there also times (ie "System Font" on macOS - that's a macOS name
                // not JavaFX System font) when the name in the font still does
                // not match what macOS told us but we need to register it anyway
                // That needs to be handled by the caller of that case - see
                // createFontResource() below.
                storeInMap(fr.getFullName(), fr);
                fileNameToFontResourceMap.put(key, fr);
            }
            return fr;
        } catch (Exception e) {
            if (PrismFontFactory.debugFonts) {
                e.printStackTrace();
            }
            return null;
        }
    }

   /*
    * Whether a ttf or ttc this will find the index of the named font.
    */
   public int findFontIndex(String targetName, String filename) {

         try {
             FontFileImpl fr = createFontFile(null, filename, 0, false, false, false, false);

             int cnt = fr.getFontCount();
             if (cnt == 1 || fr.getFullName().equalsIgnoreCase(targetName)) {
                 return 0;
             }
             int index = 1;
             do {
                 fr = createFontFile(null, filename, index, false, false, false, false);
                 String name = fr.getFullName();
                 if (name.equalsIgnoreCase(targetName)) {
                     return index;
                 }
             } while (++index < cnt);
         } catch (Exception e) {
             if (PrismFontFactory.debugFonts) {
                 e.printStackTrace();
             }
         }
         return -1;
   }

    private FontFileImpl createFontResource(String name, String filename) {
        FontFileImpl[] pffArr =
            createFontResources(name, filename, true, false, false, false, true);
        if (pffArr == null || pffArr.length == 0) {
           return null;
        } else {
           for (int i = 0; i < pffArr.length; i++) {
               if (pffArr[i].getFullName().equalsIgnoreCase(name)) {
                   return pffArr[i];
               }
           }
        }
        // No exact match - we don't want to repeat the look up so
        // log this, but store the specified name as the key to
        // match first font in this file
        storeInMap(name, pffArr[0]);
        if (PrismFontFactory.debugFonts) {
            System.err.println("No match for name " + name + " in " + filename);
        }
        return pffArr[0];
    }

    private FontFileImpl[] createFontResources(String name, String filename,
                                               boolean register,
                                               boolean embedded,
                                               boolean copy,
                                               boolean tracked,
                                               boolean loadAll) {

        FontFileImpl[] fArr = null;
        if (filename == null) {
            return null;
        }
        FontFileImpl fr = createFontResource(name, filename, 0, register,
                                              embedded, copy, tracked);
        if (fr == null) {
            return null;
        }
        int cnt = (!loadAll) ? 1 : fr.getFontCount();
        fArr = new FontFileImpl[cnt];
        fArr[0] = fr;
        if (cnt == 1) { // Not a TTC, or only requesting one font.
            return fArr;
        }
        FontFileImpl.FileRefCounter rc = null;
        if (copy) {
            rc = fr.createFileRefCounter();
        }
        int index = 1;
        do {
            String key = (filename+index).toLowerCase();
            try {
                fr = fileNameToFontResourceMap.get(key);
                if (fr != null) {
                    fArr[index] = fr;
                } else {
                    fr = createFontFile(null, filename, index,
                                        register, embedded,
                                        copy, tracked);
                    if (fr == null) {
                        return null;
                    }
                    if (rc != null) {
                        fr.setAndIncFileRefCounter(rc);
                    }
                    fArr[index] = fr;
                    String fontname = fr.getFullName();
                    if (register) {
                        storeInMap(fontname, fr);
                        fileNameToFontResourceMap.put(key, fr);
                    }
                }
            } catch (Exception e) {
                if (PrismFontFactory.debugFonts) {
                    e.printStackTrace();
                }
                return null;
            }

        } while (++index < cnt);
        return fArr;
    }

    private String dotStyleStr(boolean bold, boolean italic) {
        if (!bold) {
            if (!italic) {
                return "";
            }
            else {
                return ".italic";
            }
        } else {
            if (!italic) {
                return ".bold";
            }
            else {
                return ".bolditalic";
            }
        }
    }

    protected void storeInMap(String name, FontFile resource) {
        if (name == null || resource == null) {
            return;
        }
        if (resource instanceof PrismCompositeFontFile) {
            System.err.println(name + " is a composite " +
                                            resource);
            Thread.dumpStack();
            return;
        }
        fontResourceMap.put(name.toLowerCase(), resource);
    }

    private ArrayList<WeakReference<FontFileImpl>> tmpFonts;
    synchronized void addDecodedFont(FontFileImpl fr) {
        fr.setIsDecoded(true);
        addTmpFont(fr);
    }

    private synchronized void addTmpFont(FontFileImpl fr) {
        if (tmpFonts == null) {
            tmpFonts = new ArrayList<>();
        }
        WeakReference<FontFileImpl> ref;
        /* Registered fonts are enumerable by the application and are
         * expected to persist until VM shutdown.
         * Other fonts - notably ones temporarily loaded in a web page via
         * webview - should be eligible to be collected and have their
         * temp files deleted at any time.
         */
        if (fr.isRegistered()) {
            ref = new WeakReference<>(fr);
        } else {
            ref = fr.createFileDisposer(this, fr.getFileRefCounter());
        }
        tmpFonts.add(ref);
        addFileCloserHook();
    }

    synchronized void removeTmpFont(WeakReference<FontFileImpl> ref) {
        if (tmpFonts != null) {
            tmpFonts.remove(ref);
        }
    }

    /* familyName is expected to be a physical font family name.
     */
    public synchronized FontFile getFontResource(String familyName,
                                                 boolean bold,
                                                 boolean italic,
                                                 boolean wantComp) {

        if (familyName == null || familyName.isEmpty()) {
            return null;
        }

        String lcFamilyName = familyName.toLowerCase();
        String styleStr = dotStyleStr(bold, italic);
        FontFile fr;

        fr = lookupResource(lcFamilyName+styleStr, wantComp);
        if (fr != null) {
            return fr;
        }


        /* We may have registered this as an embedded font.
         * In which case we should also try to locate it in
         * the non-composite map before looking elsewhere.
         * First look for a font with the exact styles specified.
         * If that fails, look for any font in the family.
         * Later on this should be a lot smarter about finding the best
         * match, but that can wait until we have better style matching
         * for all cases.
         */
        if (embeddedFonts != null && wantComp) {
            fr = lookupResource(lcFamilyName+styleStr, false);
            if (fr != null) {
                return new PrismCompositeFontFile(fr, lcFamilyName+styleStr);
            }
            for (FontFileImpl embeddedFont : embeddedFonts.values()) {
                String lcEmFamily = embeddedFont.getFamilyName().toLowerCase();
                if (lcEmFamily.equals(lcFamilyName)) {
                    return new PrismCompositeFontFile(embeddedFont,
                                                        lcFamilyName+styleStr);
                }
            }
        }

        /* We have hard coded some of the most commonly used Windows fonts
         * so as to avoid the overhead of doing a lookup via GDI.
         */
        if (isWindows) {
            int style = ((bold ? 1 : 0)) + ((italic) ? 2 : 0);
            String fontFile = WindowsFontMap.findFontFile(lcFamilyName, style);
            if (fontFile != null) {
                fr = createFontResource(null, fontFile);
                if (fr != null) {
                    if (bold == fr.isBold() && italic == fr.isItalic() &&
                        !styleStr.isEmpty())
                    {
                        storeInMap(lcFamilyName+styleStr, fr);
                    }
                    if (wantComp) {  // wrap with fallback support
                        fr = new PrismCompositeFontFile(fr,
                                                       lcFamilyName+styleStr);
                    }
                    return fr;
                }
            }
        }

        getFullNameToFileMap();
        ArrayList<String> family = familyToFontListMap.get(lcFamilyName);
        if (family == null) {
            return null;
        }

        FontFile plainFR = null, boldFR = null,
            italicFR = null, boldItalicFR = null;
        for (String fontName : family) {
            String lcFontName = fontName.toLowerCase();
            fr = fontResourceMap.get(lcFontName);
            if (fr == null) {
                String file = findFile(lcFontName);
                if (file != null) {
                    fr = getFontResource(fontName, file);
                }
                if (fr == null) {
                    continue;
                }
                storeInMap(lcFontName, fr);
            }
            if (bold == fr.isBold() && italic == fr.isItalic()) {
                storeInMap(lcFamilyName+styleStr, fr);
                if (wantComp) {  // wrap with fallback support
                    fr = new PrismCompositeFontFile(fr,
                                                      lcFamilyName+styleStr);
                }
                return fr;
            }
            if (!fr.isBold()) {
                if (!fr.isItalic()) {
                    plainFR = fr;
                } else {
                    italicFR = fr;
                }
            } else {
                if (!fr.isItalic()) {
                    boldFR = fr;
                } else {
                    boldItalicFR = fr;
                }
            }
        }

        /* If get here, no perfect match in family. Substitute the
         * closest one we found.
         */
        if (!bold && !italic) {
            if (boldFR != null) {
                fr = boldFR;
            } else if (italicFR != null) {
                fr = italicFR;
            } else {
                fr = boldItalicFR;
            }
        } else if (bold && !italic) {
            if (plainFR != null) {
                fr = plainFR;
            } else if (boldItalicFR != null) {
                fr = boldItalicFR;
            } else {
                fr = italicFR;
            }
        } else if (!bold && italic) {
            if (boldItalicFR != null) {
                fr =  boldItalicFR;
            } else if (plainFR != null) {
                fr = plainFR;
            } else {
                fr = boldFR;
            }
        } else /* (bold && italic) */ {
            if (italicFR != null) {
                fr = italicFR;
            } else if (boldFR != null) {
                fr = boldFR;
            } else {
                fr = plainFR;
            }
        }
        if (fr != null) {
            storeInMap(lcFamilyName+styleStr, fr);
            if (wantComp) {  // wrap with fallback support
                fr = new PrismCompositeFontFile(fr, lcFamilyName+styleStr);
            }
        }
        return fr;
    }

    @Override
    public synchronized Font createFont(String familyName, boolean bold,
                                          boolean italic, float size) {
        FontFile fr = null;
        if (familyName != null && !familyName.isEmpty() && !isExcluded(familyName)) {
            Font logFont = LogicalFont.getLogicalFont(familyName, bold, italic, size);
            if (logFont != null) {
                return logFont;
            }
            fr = getFontResource(familyName, bold, italic, true);
        }

        if (fr == null) {
            // "System" is the default if we didn't recognise the family
            return LogicalFont.getLogicalFont("System", bold, italic, size);
        }
        return new PrismFont(fr, fr.getFullName(), size);
    }

    @Override
    public synchronized Font createFont(String name, float size) {

        FontFile fr = null;
        if (name != null && !name.isEmpty() && !isExcluded(name)) {
            Font logFont =
                LogicalFont.getLogicalFont(name, size);
            if (logFont != null) {
                return logFont;
            }

            fr = getFontResource(name, null, true);
        }
        if (fr == null) {
            return LogicalFont.getLogicalFont(DEFAULT_FULLNAME, size);
        }
        return new PrismFont(fr, fr.getFullName(), size);
    }

    private FontFileImpl getFontResource(String name, String file) {
        /* caller assures file not null */
        return createFontResource(name, file);
    }

    @Override
    public synchronized Font deriveFont(Font font, boolean bold,
                                        boolean italic, float size) {
        FontFile fr = font.getFontResource();
        //TODO honor bold and italic
        return new PrismFont(fr, fr.getFullName(), size);
    }

    private FontFile lookupResource(String lcName, boolean wantComp) {
        if (wantComp) {
            return compResourceMap.get(lcName);
        } else {
            return fontResourceMap.get(lcName);
        }
    }

    public synchronized FontFile getFontResource(String name, String file,
                                                 boolean wantComp) {
        FontFile fr = null;

        // First check if the font is already known.
        if (name != null) {
            String lcName = name.toLowerCase();

            // if requesting a wrapped resource, look in the composite map
            // else look in the physical resource map
            FontFile fontFile = lookupResource(lcName, wantComp);
            if (fontFile != null) {
                return fontFile;
            }

            /* We may have registered this as an embedded font.
             * In which case we should also try to locate it in
             * the non-composite map before looking elsewhere.
             */
            if (embeddedFonts != null && wantComp) {
                fr = lookupResource(lcName, false);
                if (fr != null) {
                    fr = new PrismCompositeFontFile(fr, lcName);
                }
                if (fr != null) {
                    return fr;
                }
            }
        }

        /* We have hard coded some of the most commonly used Windows fonts
         * so as to avoid the overhead of doing a lookup via GDI.
         */
        if (isWindows && name != null) {
            String lcName = name.toLowerCase();
            String fontFile = WindowsFontMap.findFontFile(lcName, -1);
            if (fontFile != null) {
                fr = createFontResource(null, fontFile);
                if (fr != null) {
                    if (wantComp) {
                        fr = new PrismCompositeFontFile(fr, lcName);
                    }
                    return fr;
                }
            }
        }

        getFullNameToFileMap(); // init maps

        if (name != null && file != null) {
            // Typically the TTC case used in font linking.
            // The called method adds the resources to the physical
            // map so no need  to do it here.
            fr = getFontResource(name, file);
            if (fr != null) {
                if (wantComp) {
                    fr = new PrismCompositeFontFile(fr, name.toLowerCase());
                }
                return fr;
            }
        }

        if (name != null) { // Typically normal application lookup
            fr = getFontResourceByFullName(name, wantComp);
            if (fr != null) {
                return fr;
            }
        }

        if (file != null) { // Typically the TTF case used in font linking
            fr = getFontResourceByFileName(file, wantComp);
            if (fr != null) {
                return fr;
            }
        }

        /* can't find the requested font, caller will fall back to default */
        return null;
    }

    /* To be called only by methods that already inited the maps
     */
    synchronized private FontFile
        getFontResourceByFileName(String file, boolean wantComp) {

        if (fontToFileMap.size() <= 1) {
            return null;
        }

        /* This is a little tricky: we know the file we want but we need
         * to check if its already a loaded resource. The maps are set up
         * to check if a font is loaded by its name, not file.
         * To help I added a map from file->font for all the windows fonts
         * but that is valid only for TTF fonts (not TTC). So it should only
         * be used in a context where we know its a TTF (or OTF) file.
         */
        String name = fileToFontMap.get(file.toLowerCase()); // basename
        FontFile fontFile = null;
        if (name == null) {
            // We should not normally get here with a name that we did
            // not find from the platform but any EUDC font is in the
            // list of linked fonts but it is not enumerated by Windows.
            // So we need to open the file and load it as requested.
           fontFile = createFontResource(file, 0);
           if (fontFile != null) {
               String lcName = fontFile.getFullName().toLowerCase();
               storeInMap(lcName, fontFile);
               // Checking wantComp, although the linked/fallback font
               // case doesn't use this.
                if (wantComp) {
                    fontFile =
                        new PrismCompositeFontFile(fontFile, lcName);
                }
           }
        } else {
            String lcName = name.toLowerCase();
            fontFile = lookupResource(lcName, wantComp);

            if (fontFile == null) {
                String fullPath = findFile(lcName);
                if (fullPath != null) {
                    fontFile = getFontResource(name, fullPath);
                    if (fontFile != null) {
                        storeInMap(lcName, fontFile);
                    }
                    if (wantComp) {
                        // wrap with fallback support
                        fontFile =
                                new PrismCompositeFontFile(fontFile, lcName);
                    }
                }
            }
        }
        return fontFile; // maybe null
    }

    /* To be called only by methods that already inited the maps
     * and checked the font is not already loaded.
     */
    synchronized private FontFile
        getFontResourceByFullName(String name, boolean wantComp) {

        String lcName = name.toLowerCase();

        FontFile fontFile = null;
        String file = findFile(lcName);
        if (file != null) {
            fontFile = getFontResource(name, file);
            if (fontFile != null) {
                storeInMap(lcName, fontFile);
                if (wantComp) {
                    // wrap with fallback support
                    fontFile =
                        new PrismCompositeFontFile(fontFile, lcName);
                }
            }
        }
        return fontFile;
    }

    FontFile getDefaultFontResource(boolean wantComp) {
        // TODO
        return null;
    }

    private String findFile(String name) {
        getFullNameToFileMap();
        String filename = fontToFileMap.get(name);
        if (isWindows) {
            filename = getPathNameWindows(filename);
        }
        return filename;
    }

    /* Used to indicate required return type from toArray(..); */
    private static final String[] STR_ARRAY = new String[0];

    /* Obtained from Platform APIs (windows only)
     * Map from lower-case font full name to basename of font file.
     * Eg "arial bold" -> ARIALBD.TTF.
     * For TTC files, there is a mapping for each font in the file.
     */
    private volatile HashMap<String, String> fontToFileMap = null;

    /*  TTF/OTF Font File to Font Full Name */
    private HashMap<String,String> fileToFontMap = null;

    /* Obtained from Platform APIs (windows only)
     * Map from lower-case font full name to the name of its font family
     * Eg "arial bold" -> "Arial"
     */
    private HashMap<String,String> fontToFamilyNameMap = null;

    /* Obtained from Platform APIs (windows only)
     * Map from a lower-case family name to a list of full names of
     * the member fonts, eg:
     * "arial" -> ["Arial", "Arial Bold", "Arial Italic","Arial Bold Italic"]
     */
    private HashMap<String,ArrayList<String>> familyToFontListMap= null;


    /* For a terminal server there may be two font directories */
    private static String sysFontDir = null;
    private static String userFontDir = null;

    private static native String getFontPath();

    private static void getPlatformFontDirs() {

        if (userFontDir != null || sysFontDir != null) {
            return;
        }
        String path = getFontPath();

        int scIdx = path.indexOf(';');
        if (scIdx < 0) {
            sysFontDir = path;
        } else {
            sysFontDir = path.substring(0, scIdx);
            userFontDir = path.substring(scIdx+1, path.length());
        }
    }

    /* This is needed since some windows registry names don't match
     * the font names.
     * - UPC styled font names have a double space, but the
     * registry entry mapping to a file doesn't.
     * - Marlett is in a hidden file not listed in the registry
     * - The registry advertises that the file david.ttf contains a
     * font with the full name "David Regular" when in fact its
     * just "David".
     * Directly fix up these known cases as this is faster.
     * If a font which doesn't match these known cases has no file,
     * it may be a font that has been temporarily added to the known set
     * or it may be an installed font with a missing registry entry.
     * Installed fonts are those in the windows font directories.
     * Make a best effort attempt to locate these.
     * We obtain the list of TrueType fonts in these directories and
     * filter out all the font files we already know about from the registry.
     * What remains may be "bad" fonts, duplicate fonts, or perhaps the
     * missing font(s) we are looking for.
     * Open each of these files to find out.
     */
    private void resolveWindowsFonts
        (HashMap<String,String> fontToFileMap,
         HashMap<String,String> fontToFamilyNameMap,
         HashMap<String,ArrayList<String>> familyToFontListMap) {

        ArrayList<String> unmappedFontNames = null;
        for (String font : fontToFamilyNameMap.keySet()) {
            String file = fontToFileMap.get(font);
            if (file == null) {
                int dsi = font.indexOf("  ");
                if (dsi > 0) {
                    String newName = font.substring(0, dsi);
                    newName = newName.concat(font.substring(dsi+1));
                    file = fontToFileMap.get(newName);
                    /* If this name exists and isn't for a valid name
                     * replace the mapping to the file with this font
                     */
                    if (file != null &&
                        !fontToFamilyNameMap.containsKey(newName)) {
                        fontToFileMap.remove(newName);
                        fontToFileMap.put(font, file);
                    }
                } else if (font.equals("marlett")) {
                    fontToFileMap.put(font, "marlett.ttf");
                } else if (font.equals("david")) {
                    file = fontToFileMap.get("david regular");
                    if (file != null) {
                        fontToFileMap.remove("david regular");
                        fontToFileMap.put("david", file);
                    }
                } else {
                    if (unmappedFontNames == null) {
                        unmappedFontNames = new ArrayList<>();
                    }
                    unmappedFontNames.add(font);
                }
            }
        }

        if (unmappedFontNames != null) {
            HashSet<String> unmappedFontFiles = new HashSet<>();

            // Used HashMap.clone() on SE but TV didn't support it.
            HashMap<String,String> ffmapCopy = new HashMap<>();
            ffmapCopy.putAll(fontToFileMap);
            for (String key : fontToFamilyNameMap.keySet()) {
                ffmapCopy.remove(key);
            }
            for (String key : ffmapCopy.keySet()) {
                unmappedFontFiles.add(ffmapCopy.get(key));
                fontToFileMap.remove(key);
            }
            resolveFontFiles(unmappedFontFiles,
                             unmappedFontNames,
                             fontToFileMap,
                             fontToFamilyNameMap,
                             familyToFontListMap);

            /* remove from the set of names that will be returned to the
             * user any fonts that can't be mapped to files.
             */
            if (unmappedFontNames.size() > 0) {
                int sz = unmappedFontNames.size();
                for (int i=0; i<sz; i++) {
                    String name = unmappedFontNames.get(i);
                    String familyName = fontToFamilyNameMap.get(name);
                    if (familyName != null) {
                        ArrayList<String> family = familyToFontListMap.get(familyName);
                        if (family != null) {
                            if (family.size() <= 1) {
                                familyToFontListMap.remove(familyName);
                            }
                        }
                    }
                    fontToFamilyNameMap.remove(name);
                }
            }
        }
    }

    private void resolveFontFiles(HashSet<String> unmappedFiles,
         ArrayList<String> unmappedFonts,
         HashMap<String,String> fontToFileMap,
         HashMap<String,String> fontToFamilyNameMap,
         HashMap<String,ArrayList<String>> familyToFontListMap) {

        for (String file : unmappedFiles) {
            try {
                int fn = 0;
                FontFileImpl ttf;
                String fullPath = getPathNameWindows(file);
                do {
                    ttf = createFontResource(fullPath, fn++);
                    if (ttf == null) {
                        break;
                    }
                    String fontNameLC = ttf.getFullName().toLowerCase();
                    String localeNameLC =ttf.getLocaleFullName().toLowerCase();
                    if (unmappedFonts.contains(fontNameLC) ||
                        unmappedFonts.contains(localeNameLC)) {
                        fontToFileMap.put(fontNameLC, file);
                        unmappedFonts.remove(fontNameLC);
                        /* If GDI reported names using locale specific style
                         * strings we'll have those as the unmapped keys in
                         * the font to family list and also in the value
                         * array list mapped by the family.
                         * We can spot these if the localeName is what is
                         * actually in the unmapped font list, and we'll
                         * then replace all occurrences of the locale name with
                         * the English name.
                         */
                        if (unmappedFonts.contains(localeNameLC)) {
                            unmappedFonts.remove(localeNameLC);
                            String family = ttf.getFamilyName();
                            String familyLC = family.toLowerCase();
                            fontToFamilyNameMap.remove(localeNameLC);
                            fontToFamilyNameMap.put(fontNameLC, family);
                            ArrayList<String> familylist =
                                familyToFontListMap.get(familyLC);
                            if (familylist != null) {
                                familylist.remove(ttf.getLocaleFullName());
                            } else {
                                /* The family name was not English.
                                 * Remove the non-English family list
                                 * and replace it with the English one
                                 */
                                String localeFamilyLC =
                                    ttf.getLocaleFamilyName().toLowerCase();
                                familylist =
                                    familyToFontListMap.get(localeFamilyLC);
                                if (familylist != null) {
                                    familyToFontListMap.remove(localeFamilyLC);
                                }
                                familylist = new ArrayList<>();
                                familyToFontListMap.put(familyLC, familylist);
                            }
                            familylist.add(ttf.getFullName());
                        }
                    }

                }
                while (fn < ttf.getFontCount());
            } catch (Exception e) {
                if (debugFonts) {
                    e.printStackTrace();
                }
            }
        }
    }

    static native void
        populateFontFileNameMap(HashMap<String,String> fontToFileMap,
                                 HashMap<String,String> fontToFamilyNameMap,
                                 HashMap<String,ArrayList<String>>
                                     familyToFontListMap,
                                 Locale locale);

    protected static String getPathNameWindows(final String filename) {
        if (filename == null) {
            return null;
        }

        getPlatformFontDirs();
        File f = new File(filename);
        if (f.isAbsolute()) {
            return filename;
        }
        if (userFontDir == null) {
            return sysFontDir+"\\"+filename;
        }

        @SuppressWarnings("removal")
        String path = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                @Override
                public String run() {
                    File f = new File(sysFontDir+"\\"+filename);
                    if (f.exists()) {
                        return f.getAbsolutePath();
                    }
                    else {
                        return userFontDir+"\\"+filename;
                    }
                }
            });

            if (path != null) {
                return path;
            }
        return null; //  shouldn't happen.
    }

    /*
     * Do not return matching names via public API.
     * Some names may be needed for internal matching but
     * never exposed to application code.
     * Initially this only excludes certain names on macOS.
     */
    public boolean isExcluded(String name) {
        return false;
    }

    private static ArrayList<String> allFamilyNames;
    @Override
    public String[] getFontFamilyNames() {
        if (allFamilyNames == null) {
            /* Create an array list and add the families for :
             * - logical fonts
             * - Embedded fonts
             * - Fonts found on the platform (includes JRE fonts)..
             */
            ArrayList<String> familyNames = new ArrayList<>();
            LogicalFont.addFamilies(familyNames);
            //  Putting this in here is dependendent on the FontLoader
            // loading embedded fonts before calling into here. If
            // embedded fonts can be added then we need to add these
            // dynamically for each call to this method.

            if (embeddedFonts != null) {
                for (FontFileImpl embeddedFont : embeddedFonts.values()) {
                    if (!familyNames.contains(embeddedFont.getFamilyName())) {
                        familyNames.add(embeddedFont.getFamilyName());
                    }
                }
            }
            getFullNameToFileMap();
            for (String f : fontToFamilyNameMap.values()) {
                if (!familyNames.contains(f)) {
                    if (!isExcluded(f)) {
                        familyNames.add(f);
                    }
                }
            }
            Collections.sort(familyNames);
            allFamilyNames = new ArrayList<>(familyNames);
        }
        return allFamilyNames.toArray(STR_ARRAY);
    }

    private static ArrayList<String> allFontNames;
    @Override
    public String[] getFontFullNames() {
        if (allFontNames == null) {
            /* Create an array list and add
             * - logical fonts
             * - Embedded fonts
             * - Fonts found on the platform (includes JRE fonts).
             */
            ArrayList<String> fontNames = new ArrayList<>();
            LogicalFont.addFullNames(fontNames);
            if (embeddedFonts != null) {
                for (FontFileImpl embeddedFont : embeddedFonts.values()) {
                    if (!fontNames.contains(embeddedFont.getFullName())) {
                        fontNames.add(embeddedFont.getFullName());
                    }
                }
            }
            getFullNameToFileMap();
            for (ArrayList<String> a : familyToFontListMap.values()) {
                for (String s : a) {
                    if (!isExcluded(s)) {
                        fontNames.add(s);
                    }
                }
            }
            Collections.sort(fontNames);
            allFontNames = fontNames;
        }
        return allFontNames.toArray(STR_ARRAY);
    }

    @Override
    public String[] getFontFullNames(String family) {

        if (isExcluded(family)) {
            return STR_ARRAY;
        }
        // First check if its a logical font family.
        String[] logFonts = LogicalFont.getFontsInFamily(family);
        if (logFonts != null) {
            // Caller will clone/wrap this before returning it to API
            return logFonts;
        }
        // Next check if its an embedded font family
        if (embeddedFonts != null) {
            ArrayList<String> embeddedFamily = null;
            for (FontFileImpl embeddedFont : embeddedFonts.values()) {
                if (embeddedFont.getFamilyName().equalsIgnoreCase(family)) {
                    if (embeddedFamily == null) {
                        embeddedFamily = new ArrayList<>();
                    }
                    embeddedFamily.add(embeddedFont.getFullName());
                }
            }
            if (embeddedFamily != null) {
                return embeddedFamily.toArray(STR_ARRAY);
            }
        }

        getFullNameToFileMap();
        family = family.toLowerCase();
        ArrayList<String> familyFonts = familyToFontListMap.get(family);
        if (familyFonts != null) {
            return familyFonts.toArray(STR_ARRAY);
        } else {
            return STR_ARRAY; // zero-length therefore immutable.
        }
    }

    @Override
    public boolean isPlatformFont(String name) {
        if (name == null) {
            return false;
        }
        /* Using String#startsWith as name can be either a fullName or a family name */
        String lcName = name.toLowerCase();
        if (LogicalFont.isLogicalFont(lcName)) {
            return true;
        }
        String systemFamily = getSystemFont(LogicalFont.SYSTEM).toLowerCase();
        return lcName.startsWith(systemFamily);
    }

    private static Thread fileCloser = null;

    private synchronized void addFileCloserHook() {
        if (fileCloser == null) {
            final Runnable fileCloserRunnable = () -> {
                if (embeddedFonts != null) {
                    for (FontFileImpl font : embeddedFonts.values()) {
                        font.disposeOnShutdown();
                    }
                }
                if (tmpFonts != null) {
                    for (WeakReference<FontFileImpl> ref : tmpFonts) {
                        FontFileImpl font = ref.get();
                        if (font != null) {
                            font.disposeOnShutdown();
                        }
                    }
                }
            };
            @SuppressWarnings("removal")
            Object dummy = java.security.AccessController.doPrivileged(
                    (PrivilegedAction<Object>) () -> {
                        /* The thread must be a member of a thread group
                         * which will not get GCed before VM exit.
                         * Make its parent the top-level thread group.
                         */
                        ThreadGroup tg = Thread.currentThread().getThreadGroup();
                        for (ThreadGroup tgn = tg;
                             tgn != null; tg = tgn, tgn = tg.getParent());
                        fileCloser = new Thread(tg, fileCloserRunnable);
                        fileCloser.setContextClassLoader(null);
                        Runtime.getRuntime().addShutdownHook(fileCloser);
                        return null;
                    }
            );
        }
    }

    private HashMap<String, FontFileImpl> embeddedFonts;

    @Override
    public Font[] loadEmbeddedFont(String name, InputStream fontStream,
                                     float size,
                                     boolean register,
                                     boolean loadAll) {
        if (!hasPermission()) {
            return new Font[] { createFont(DEFAULT_FULLNAME, size) } ;
        }
        if (FontFileWriter.hasTempPermission()) {
            return loadEmbeddedFont0(name, fontStream, size, register, loadAll);
        }

        // Otherwise, be extra conscious of pending temp file creation and
        // resourcefully handle the temp file resources, among other things.
        FontFileWriter.FontTracker tracker =
            FontFileWriter.FontTracker.getTracker();
        boolean acquired = false;
        try {
            acquired = tracker.acquirePermit();
            if (!acquired) {
                // Timed out waiting for resources.
                return null;
            }
            return loadEmbeddedFont0(name, fontStream, size, register, loadAll);
        } catch (InterruptedException e) {
            // Interrupted while waiting to acquire a permit.
            return null;
        } finally {
            if (acquired) {
                tracker.releasePermit();
            }
        }
    }

    private Font[] loadEmbeddedFont0(String name, InputStream fontStream,
                                       float size,
                                       boolean register,
                                       boolean loadAll) {
        FontFileImpl[] fr = null;
        FontFileWriter fontWriter = new FontFileWriter();
        try {
            // We use a shutdown hook to close all open tmp files
            // created via this API and delete them.
            final File tFile = fontWriter.openFile();
            byte[] buf = new byte[8192];
            for (;;) {
                int bytesRead = fontStream.read(buf);
                if (bytesRead < 0) {
                    break;
                }
                fontWriter.writeBytes(buf, 0, bytesRead);
            }
            fontWriter.closeFile();

            fr = loadEmbeddedFont1(name, tFile.getPath(), register, true,
                                   fontWriter.isTracking(), loadAll);

            if (fr != null && fr.length > 0) {
                /* Delete the file downloaded if it was decoded
                 * to another file */
                if (fr[0].isDecoded()) {
                    fontWriter.deleteFile();
                }
            }

            /* We don't want to leave the temp files around after exit.
             * Also fonts can be over-written by new versions and
             * need to be cleaned up.
             * We also need to decrement the byte count by the size
             * of the file.
             */
            addFileCloserHook();
        } catch (Exception e) {
            fontWriter.deleteFile();
        } finally {
            /* If the data isn't a valid font, so that registering it
             * returns null, or we didn't get so far as copying the data,
             * delete the tmp file and decrement the byte count
             * in the tracker object before returning.
             */
            if (fr == null) {
                fontWriter.deleteFile();
            }
        }
        if (fr != null && fr.length > 0) {
            if (size <= 0) {
                size = getSystemFontSize();
            }
            int num = fr.length;
            PrismFont[] pFonts = new PrismFont[num];
            for (int i=0; i<num; i++) {
                pFonts[i] = new PrismFont(fr[i], fr[i].getFullName(), size);
            }
            return pFonts;
        }
        return null;
    }

    /**
     * registerEmbeddedFont(String name, String path) is a small subset of
     * registerEmbeddedFont(String name, InputStream fontStream)
     * It does not attempt to create a temporary file and has different
     * parameters.
     *
     * @param name font name
     * @param path Path name to system file
     * @param size font size
     * @param register whether the font should be registered.
     * @param loadAll whether to load all fonts if it is a TTC
     * @return font name extracted from font file
     */
    @Override
    public Font[] loadEmbeddedFont(String name, String path,
                                     float size,
                                     boolean register,
                                     boolean loadAll) {
        if (!hasPermission()) {
            return new Font[] { createFont(DEFAULT_FULLNAME, size) };
        }
        addFileCloserHook();
        FontFile[] frArr =
          loadEmbeddedFont1(name, path, register, false, false, loadAll);
        if (frArr != null && frArr.length > 0) {
            if (size <= 0) {
                size = getSystemFontSize();
            }
            int num = frArr.length;
            Font[] fonts = new Font[num];
            for (int i=0; i<num; i++) {
                fonts[i] = new PrismFont(frArr[i], frArr[i].getFullName(), size);
            }
            return fonts;
        }
        return null;
    }

    /* This should make the embedded font eligible for reclaimation
     * and subsequently, disposal of native resources, once any existing
     * strong refs by the application are released.
     */
    private void removeEmbeddedFont(String name) {
        FontFileImpl font = embeddedFonts.get(name);
        if (font == null) {
            return;
        }
        embeddedFonts.remove(name);
        String lcName = name.toLowerCase();
        fontResourceMap.remove(lcName);
        compResourceMap.remove(lcName);
        // The following looks tedious, but if the compMap could have
        // the font referenced via some lookup name that applies a style
        // or used the family name, we need to find it and remove all
        // references to it, so it can be collected.
        Iterator<CompositeFontFile> fi = compResourceMap.values().iterator();
            while (fi.hasNext()) {
            CompositeFontFile compFont = fi.next();
            if (compFont.getSlotResource(0) == font) {
                fi.remove();
            }
        }
    }

    protected boolean registerEmbeddedFont(String path) {
        return true;
    }

    // Used for testing
    private int numEmbeddedFonts = 0;
    public int test_getNumEmbeddedFonts() {
        return numEmbeddedFonts;
    }

    private synchronized
        FontFileImpl[] loadEmbeddedFont1(String name, String path,
                                         boolean register, boolean copy,
                                         boolean tracked, boolean loadAll) {

        ++numEmbeddedFonts;
        /*
         * Fonts that aren't platform installed include those in the
         * application jar, WOFF fonts that are downloaded, and fonts
         * created via Font.loadFont. If copy==true, we can infer its
         * one of these, but its also possible for a font to be file-system
         * installed as part of the application but not known to the
         * platform. In this case copy==false, but we still need to flag
         * to the system its not a platform font so that other pipelines
         * know to reference the file directly.
         */
        FontFileImpl[] frArr = createFontResources(name, path, register,
                                                    true, copy, tracked,
                                                    loadAll);
        if (frArr == null || frArr.length == 0) {
            return null; // yes, this means the caller needs to handle null.
        }

        /* Before we return or register, make sure names are present
         * check whether any of the fonts duplicate an OS font.
         */

        if (embeddedFonts == null) {
            embeddedFonts = new HashMap<>();
        }

        boolean registerEmbedded = true;
        for (int i=0; i<frArr.length; i++) {
            FontFileImpl fr = frArr[i];
            String family = fr.getFamilyName();
            if (family == null || family.length() == 0) {
                return null;
            }
            String fullname = fr.getFullName();
            if (fullname == null || fullname.length() == 0) {
                return null;
            }
            String psname = fr.getPostscriptName();
            if (psname == null || psname.length() == 0) {
                return null;
            }

            FontFile resource = embeddedFonts.get(fullname);
            if (resource != null && fr.equals(resource)) {
                /* Do not register the same font twice in the OS */
                registerEmbedded = false;
            }
        }

        if (registerEmbedded) {
            /* Use filename from the resource so woff fonts are handled */
            if (!registerEmbeddedFont(frArr[0].getFileName())) {
                /* This font file can't be used by the underlying rasterizer */
                return null;
            }
        }

        /* If a temporary font is a copy but it is not decoded then it
         * will not be anywhere the shutdown hook can see.
         * That means if the font is keep for the entire life of the VM
         * its file will not be deleted.
         * The fix is to add this font to the list of temporary fonts.
         */
        if (copy && !frArr[0].isDecoded()) {
            addTmpFont(frArr[0]);
        }

        if (!register) {
            return frArr;
        }

        /* If a font name is provided then we will also store that in the
         * map as an alias, otherwise should use the only the real name,
         * REMIND: its possible that either name may hide some installed
         * version of the font, possibly one we haven't seen yet. But
         * without loading all the platform fonts (expensive) this is
         * difficult to ascertain. A contains() check here is therefore
         * probably mostly futile.
         */
        if (name != null && !name.isEmpty()) {
            embeddedFonts.put(name, frArr[0]);
            storeInMap(name, frArr[0]);
        }

        for (int i=0; i<frArr.length; i++) {
            FontFileImpl fr = frArr[i];
            String family = fr.getFamilyName();
            String fullname = fr.getFullName();
            removeEmbeddedFont(fullname);
            embeddedFonts.put(fullname, fr);
            storeInMap(fullname, fr);
            family = family + dotStyleStr(fr.isBold(), fr.isItalic());
            storeInMap(family, fr);
            /* The remove call is to assist the case where we have
             * previously mapped into the composite map a different style
             * in this family as a partial match for the application request.
             * This can occur when an application requested a bold font before
             * it called Font.loadFont to register the bold font. It won't
             * fix the cases that already happened, but will fix the future ones.
             */
            compResourceMap.remove(family.toLowerCase());
        }
        return frArr;
    }

    private void
        logFontInfo(String message,
                    HashMap<String,String> fontToFileMap,
                    HashMap<String,String> fontToFamilyNameMap,
                    HashMap<String,ArrayList<String>> familyToFontListMap) {

        System.err.println(message);
        for (String keyName : fontToFileMap.keySet()) {
            System.err.println("font="+keyName+" file="+
                               fontToFileMap.get(keyName));
        }
        for (String keyName : fontToFamilyNameMap.keySet()) {
            System.err.println("font="+keyName+" family="+
                               fontToFamilyNameMap.get(keyName));
        }
        for (String keyName : familyToFontListMap.keySet()) {
            System.err.println("family="+keyName+ " fonts="+
                               familyToFontListMap.get(keyName));
        }
    }

    private synchronized HashMap<String,String> getFullNameToFileMap() {
        if (fontToFileMap == null) {

            HashMap<String, String> tmpFontToFileMap = new HashMap<>(100);
            fontToFamilyNameMap = new HashMap<>(100);
            familyToFontListMap = new HashMap<>(50);
            fileToFontMap = new HashMap<>(100);

            if (isWindows) {
                getPlatformFontDirs();
                populateFontFileNameMap(tmpFontToFileMap,
                                        fontToFamilyNameMap,
                                        familyToFontListMap,
                                        Locale.ENGLISH);

                resolveWindowsFonts(tmpFontToFileMap,
                                    fontToFamilyNameMap,
                                    familyToFontListMap);

                if (debugFonts) {
                    logFontInfo(" *** WINDOWS FONTS AFTER RESOLVING",
                                tmpFontToFileMap,
                                fontToFamilyNameMap,
                                familyToFontListMap);
                }

            } else if (isMacOSX || isIOS) {
                MacFontFinder.populateFontFileNameMap(tmpFontToFileMap,
                                                      fontToFamilyNameMap,
                                                      familyToFontListMap,
                                                      Locale.ENGLISH);

                if (debugFonts) {
                    logFontInfo(" *** MACOS LOCATED FONTS:",
                                tmpFontToFileMap,
                                fontToFamilyNameMap,
                                familyToFontListMap);
                }
            } else if (isLinux) {
//                FontConfigManager.populateMaps(tmpFontToFileMap,
//                                               fontToFamilyNameMap,
//                                               familyToFontListMap,
//                                               Locale.getDefault());
                if (debugFonts) {
                    logFontInfo(" *** FONTCONFIG LOCATED FONTS:",
                                tmpFontToFileMap,
                                fontToFamilyNameMap,
                                familyToFontListMap);
                }
            } else if (isAndroid) {
               AndroidFontFinder.populateFontFileNameMap(tmpFontToFileMap,
                        fontToFamilyNameMap,
                        familyToFontListMap,
                        Locale.ENGLISH);
            } else { /* unrecognised OS */
                fontToFileMap = tmpFontToFileMap;
                return fontToFileMap;
            }

            /* Reverse map from file to font. file name is base name
             * not a full path.
             */
            for (String font : tmpFontToFileMap.keySet()) {
                String file = tmpFontToFileMap.get(font);
                fileToFontMap.put(file.toLowerCase(), font);
            }

            fontToFileMap = tmpFontToFileMap;
            if (isAndroid) {
                populateFontFileNameMapGeneric(
                       AndroidFontFinder.getSystemFontsDir());
            }

//             for (String keyName : fontToFileMap.keySet()) {
//               System.out.println("font="+keyName+" file="+ fontToFileMap.get(keyName));
//             }

//             for (String keyName : familyToFontListMap.keySet()) {
//               System.out.println("family="+keyName);
//             }
        }
        return fontToFileMap;
    }

    @Override
    @SuppressWarnings("removal")
    public final boolean hasPermission() {
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
//                sm.checkPermission(LOAD_FONT_PERMISSION);
            }
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }

    private static class TTFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir,String name) {
            /* all conveniently have the same suffix length */
            int offset = name.length()-4;
            if (offset <= 0) { /* must be at least A.ttf */
                return false;
            } else {
                return(name.startsWith(".ttf", offset) ||
                       name.startsWith(".TTF", offset) ||
                       name.startsWith(".ttc", offset) ||
                       name.startsWith(".TTC", offset) ||
                       name.startsWith(".otf", offset) ||
                       name.startsWith(".OTF", offset));
            }
        }

        private TTFilter() {
        }

        static TTFilter ttFilter;
        static TTFilter getInstance() {
            if (ttFilter == null) {
                ttFilter = new TTFilter();
            }
            return ttFilter;
        }
    }

    void addToMaps(FontFileImpl fr) {

        if (fr == null) {
            return;
        }

        String fullName = fr.getFullName();
        String familyName = fr.getFamilyName();

        if (fullName == null || familyName == null) {
            return;
        }

        String lcFullName = fullName.toLowerCase();
        String lcFamilyName = familyName.toLowerCase();

        fontToFileMap.put(lcFullName, fr.getFileName());
        fontToFamilyNameMap.put(lcFullName, familyName);
        ArrayList<String> familyList = familyToFontListMap.get(lcFamilyName);
        if (familyList == null) {
            familyList = new ArrayList<>();
            familyToFontListMap.put(lcFamilyName, familyList);
        }
        familyList.add(fullName);
    }

    void populateFontFileNameMapGeneric(String fontDir) {
        final File dir = new File(fontDir);
        String[] files = null;
        try {
            @SuppressWarnings("removal")
            String[] tmp = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<String[]>) () -> dir.list(TTFilter.getInstance())
            );
            files = tmp;
        } catch (Exception e) {
        }

        if (files == null) {
            return;
        }

        for (int i=0;i<files.length;i++) {
            try {
                String path = fontDir+File.separator+files[i];

                /* Use filename from the resource so woff fonts are handled */
                if (!registerEmbeddedFont(path)) {
                    /* This font file can't be used by the underlying rasterizer */
                    continue;
                }

                int index = 0;
                FontFileImpl fr = createFontResource(path, index++);
                if (fr == null) {
                    continue;
                }
                addToMaps(fr);
                while (index < fr.getFontCount()) {
                    fr = createFontResource(path, index++);
                    if (fr == null) {
                        break;
                    }
                    addToMaps(fr);
                }
            } catch (Exception e) {
                /* Keep going if anything bad happens with a font */
            }
        }
    }

    private static native float getSystemFontSizeNative();
    private static native String getSystemFontNative();
    private static float systemFontSize;
    private static String systemFontFamily = null;
    private static String monospaceFontFamily = null;

    public static float getSystemFontSize() {
        if (systemFontSize == -1) {
            if (isWindows) {
                systemFontSize = getSystemFontSizeNative();
            } else if (isMacOSX || isIOS) {
                systemFontSize = MacFontFinder.getSystemFontSize();
            } else if (isAndroid) {
               systemFontSize = AndroidFontFinder.getSystemFontSize();
            } else {
                systemFontSize = 13f; // Gnome uses 13.
            }
        }
        return systemFontSize;
    }

    /* Applies to Windows and Mac. Not used on Linux */
    public static String getSystemFont(String name) {
        if (name.equals(LogicalFont.SYSTEM)) {
            if (systemFontFamily == null) {
                if (isWindows) {
                    systemFontFamily = "Arial"; // play it safe.
                } else if (isMacOSX || isIOS) {
                    systemFontFamily = "Lucida Grande";
                } else if (isAndroid) {
                    systemFontFamily = AndroidFontFinder.getSystemFont();
                } else {
                    systemFontFamily = "Lucida Sans"; // for now.
                }
            }
            return systemFontFamily;
        } else if (name.equals(LogicalFont.SANS_SERIF)) {
            return "Arial";
        } else if (name.equals(LogicalFont.SERIF)) {
            return "Times New Roman";
        } else /* if (name.equals(LogicalFont.MONOSPACED)) */ {
            if (monospaceFontFamily == null) {
                monospaceFontFamily = "Courier New";
            }
            return monospaceFontFamily;
        }
    }

    public abstract FontFallbackInfo getFallbacks(FontFile primaryResource);
}