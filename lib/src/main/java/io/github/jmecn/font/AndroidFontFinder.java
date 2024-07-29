package io.github.jmecn.font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class AndroidFontFinder reads font descriptor from
 * /system/etc/system_fonts.xml. If that file doesn't exist it is replaced by
 * embedded font descriptor {@link com/sun/javafx/font/android_system_fonts.xml} which
 * defines some basic mappings based on best guess which fonts are mandatory on
 * platforms lower than 4.0 and how they map to typefaces.
 */
class AndroidFontFinder {

    private static final String SYSTEM_FONT_NAME    = "sans serif";
    private static final float SYSTEM_FONT_SIZE     = 16.0f;

    static final String fontDescriptor_2_X_Path = "/com/sun/javafx/font/android_system_fonts.xml";
    static final String fontDescriptor_4_X_Path = "/system/etc/system_fonts.xml";
    static final String systemFontsDir = "/system/fonts";

    public static String getSystemFont() {
        return SYSTEM_FONT_NAME;
    }

    public static float getSystemFontSize() {
        return SYSTEM_FONT_SIZE;
    }

    public static String getSystemFontsDir() {
        return systemFontsDir;
    }

    private static boolean parse_2_X_SystemDefaultFonts(
            final HashMap<String, String> fontToFileMap,
            final HashMap<String, String> fontToFamilyNameMap,
            final HashMap<String, ArrayList<String>> familyToFontListMap) {

        InputStream is = AndroidFontFinder.class
                .getResourceAsStream(fontDescriptor_2_X_Path);
        if (is == null) {
            System.err.println("Resource not found: " + fontDescriptor_2_X_Path);
            return false;
        }
        return parseSystemDefaultFonts(is, fontToFileMap, fontToFamilyNameMap,
                familyToFontListMap);
    }

    private static boolean parse_4_X_SystemDefaultFonts(
            final HashMap<String, String> fontToFileMap,
            final HashMap<String, String> fontToFamilyNameMap,
            final HashMap<String, ArrayList<String>> familyToFontListMap) {
        File iFile = new File(fontDescriptor_4_X_Path);
        try {
            return parseSystemDefaultFonts(new FileInputStream(iFile),
                    fontToFileMap, fontToFamilyNameMap, familyToFontListMap);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + fontDescriptor_4_X_Path);
        }
        return false;
    }

    private static boolean parseSystemDefaultFonts(final InputStream is,
            final HashMap<String, String> fontToFileMap,
            final HashMap<String, String> fontToFamilyNameMap,
            final HashMap<String, ArrayList<String>> familyToFontListMap) {

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                private final static char DASH      = '-';
                private final static String FAMILY  = "family";

                private final static String FILE    = "file";
                private final static String FILESET = "fileset";
                private final static String NAME    = "name";
                private final static String NAMESET = "nameset";
                private final static char SPACE     = ' ';
                final List<String> filesets = new ArrayList<>();

                boolean inFamily = false;
                boolean inFile = false;
                boolean inFileset = false;
                boolean inName = false;
                boolean inNameset = false;

                private final List<String> namesets = new ArrayList<>();
                private final String[] styles = new String[] {
                        "regular", "bold", "italic", "bold italic" };

                @Override
                public void characters(char[] ch, int start, int length)
                        throws SAXException {
                    if (inName) {
                        String nameset = new String(ch, start, length)
                                .toLowerCase();
                        namesets.add(nameset);
                    } else if (inFile) {
                        String fileset = new String(ch, start, length);
                        filesets.add(fileset);
                    }
                }

                @Override
                public void endElement(String uri, String localName,
                        String qName) throws SAXException {
                    if (qName.equalsIgnoreCase(FAMILY)) {
                        for (String family : namesets) {
                            int i = 0;
                            String familyName = family.replace(DASH, SPACE);
                            for (String file : filesets) {
                                String fullName = familyName + " " + styles[i];
                                String fullFile = systemFontsDir
                                        + File.separator + file;
                                File f = new File(fullFile);
                                if (!f.exists() || !f.canRead()) {
                                    continue;
                                }
                                fontToFileMap.put(fullName, fullFile);
                                fontToFamilyNameMap.put(fullName, familyName);
                                ArrayList<String> list = familyToFontListMap
                                        .get(familyName);
                                if (list == null) {
                                    list = new ArrayList<>();
                                    familyToFontListMap.put(familyName, list);
                                }
                                list.add(fullName);
                                i++;
                            }
                        }
                        inFamily = false;
                    } else if (qName.equalsIgnoreCase(NAMESET)) {
                        inNameset = false;
                    } else if (qName.equalsIgnoreCase(FILESET)) {
                        inFileset = false;
                    } else if (qName.equalsIgnoreCase(NAME)) {
                        inName = false;
                    } else if (qName.equalsIgnoreCase(FILE)) {
                        inFile = false;
                    }
                }

                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {
                    if (qName.equalsIgnoreCase(FAMILY)) {
                        inFamily = true;
                        namesets.clear();
                        filesets.clear();
                    } else if (qName.equalsIgnoreCase(NAMESET)) {
                        inNameset = true;
                    } else if (qName.equalsIgnoreCase(FILESET)) {
                        inFileset = true;
                    } else if (qName.equalsIgnoreCase(NAME)) {
                        inName = true;
                    } else if (qName.equalsIgnoreCase(FILE)) {
                        inFile = true;
                    }
                }
            };// DefaultHandler

            saxParser.parse(is, handler);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to load default fonts descriptor: "
                    + fontDescriptor_4_X_Path);
        } catch (Exception e) {
            System.err.println("Failed parsing default fonts descriptor;");
            e.printStackTrace();
        }
        return false;
    }

    public static void populateFontFileNameMap(
            HashMap<String, String> fontToFileMap,
            HashMap<String, String> fontToFamilyNameMap,
            HashMap<String, ArrayList<String>> familyToFontListMap,
            Locale locale) {

        if (fontToFileMap == null || fontToFamilyNameMap == null
                || familyToFontListMap == null) {
            return;
        }
        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        boolean systemFonts_4_X_DescriptorFound = parse_4_X_SystemDefaultFonts(
                fontToFileMap, fontToFamilyNameMap, familyToFontListMap);
        if (!systemFonts_4_X_DescriptorFound) {
            parse_2_X_SystemDefaultFonts(fontToFileMap, fontToFamilyNameMap,
                    familyToFontListMap);
        }
    }
}