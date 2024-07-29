package io.github.jmecn.font.sfntly;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Locale;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestSfntly {

    @Test void testScanSystemFonts() {
        Platform platform = JmeSystem.getPlatform();

        String[] systemFontDir = null;
        switch (platform.getOs()) {
            case Windows: {
                String windir = System.getenv("windir");
                if (windir != null) {
                    systemFontDir = new String[]{
                            windir + "\\Fonts\\",
                    };
                }
                break;
            }
            case Linux: {
                systemFontDir = new String[]{
                        "/usr/share/fonts/",
                        "/usr/share/X11/fonts/",
                        "/usr/local/share/fonts/",
                        "~/.fonts/"
                };
                break;
            }
            case MacOS: {
                systemFontDir = new String[]{
                        "/System/Library/Fonts/",
                        "/Library/Fonts/",
                        "~/Library/Fonts/"
                };
                break;
            }
            case Android: {
                systemFontDir = new String[]{
                        "/system/fonts/"
                };
            }
        }

        for (String path : systemFontDir) {

            File dir = new File(path);
            if (!dir.exists()) {
                continue;
            }

            travelDir(dir);
        }
    }

    private void travelDir(File dir) {
        File[] files = dir.listFiles(filter);
        for (File file : files) {
            testReadTTF(file);
        }
        File[] dirs = dir.listFiles(File::isDirectory);
        for (File dir1 : dirs) {
            travelDir(dir1);
        }
    }

    FilenameFilter filter = (dir, name) -> {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(".ttf") || lowerName.endsWith(".otf") || lowerName.endsWith(".ttc") || lowerName.endsWith(".otc");
    };

    void testReadTTF(File file) {

        try {
            SfntlyFontFile fontFile = new SfntlyFontFile(file.getName(), file.getAbsolutePath(), 0, false, false, false, false);
            System.out.println(fontFile);
        } catch (Exception e) {
            System.err.println("File:" + file);
            e.printStackTrace();
        }
    }

    @Test void getLocale() {
        Locale locale = Locale.getDefault();
        System.out.println(locale);
    }
}
