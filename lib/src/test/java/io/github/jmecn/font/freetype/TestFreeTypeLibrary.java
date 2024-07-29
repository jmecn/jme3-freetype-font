package io.github.jmecn.font.freetype;

import com.jme3.texture.Image;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestFreeTypeLibrary {

    static Logger log = LoggerFactory.getLogger(TestFreeTypeLibrary.class);

    @Test void testLoad() {
        Assertions.assertDoesNotThrow(() -> {
            try (FtLibrary library = new FtLibrary()) {
                log.info("Version: {}", library.getVersion());
            }
        });
    }


    @Test void loadFontStream() {
        try (FtLibrary library = new FtLibrary()){
            FileInputStream is = new FileInputStream(new File("../font/NotoSerifSC-Regular.otf"));
            FtFace face = library.newFace(is);
            is.close();

            face.setPixelSize(0, 16);
            FtGlyphSlot glyphSlot = face.getGlyphSlot();
            FtGlyphMetrics metrics = glyphSlot.getMetrics();
            FtBitmap bitmap = glyphSlot.getBitmap();

            String str = "你好，世界！Hello, World!";
            log.info("str length:{}", str.length());
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                log.info("character: {}", ch);
                int codepoint = Character.codePointAt(str, i);
                int glyphIndex = face.getCharIndex(codepoint);
                face.loadGlyph(glyphIndex, FT_LOAD_NO_BITMAP);

                log.info("slot glyph, codepoint:{}, glyphIndex:{}, index:{}", codepoint, glyphIndex, glyphSlot.getGlyphIndex());
                log.info("slot metrics, width:{}, height:{}", metrics.getWidth(), metrics.getHeight());

                face.renderGlyph(FT_RENDER_MODE_NORMAL);
                log.info("slot bitmap, numGrays:{}, pixelMode:{}, format:{}, width:{}, row:{}, pitch:{}, bufferSize:{}", bitmap.getNumGrays(), bitmap.getPixelMode(), getFormat(bitmap.getPixelMode()), bitmap.getWidth(), bitmap.getRows(), bitmap.getPitch(), bitmap.getBufferSize());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test void loadFont() {
        try (FtLibrary library = new FtLibrary()){
            FtFace face = library.newFace("../font/unifont-15.1.05.otf");
            face.setPixelSize(0, 16);
            FtGlyphSlot glyphSlot = face.getGlyphSlot();
            FtGlyphMetrics metrics = glyphSlot.getMetrics();
            FtBitmap bitmap = glyphSlot.getBitmap();

            String str = "你好，世界！Hello, World!";
            log.info("str length:{}", str.length());
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                log.info("character: {}", ch);
                int codepoint = Character.codePointAt(str, i);
                int glyphIndex = face.getCharIndex(codepoint);
                face.loadGlyph(glyphIndex, FT_LOAD_NO_BITMAP);

                log.info("slot glyph, codepoint:{}, glyphIndex:{}, index:{}", codepoint, glyphIndex, glyphSlot.getGlyphIndex());
                log.info("slot metrics, width:{}, height:{}", metrics.getWidth(), metrics.getHeight());

                face.renderGlyph(FT_RENDER_MODE_NORMAL);
                log.info("slot bitmap, numGrays:{}, pixelMode:{}, format:{}, width:{}, row:{}, pitch:{}, bufferSize:{}", bitmap.getNumGrays(), bitmap.getPixelMode(), getFormat(bitmap.getPixelMode()), bitmap.getWidth(), bitmap.getRows(), bitmap.getPitch(), bitmap.getBufferSize());

            }
        }
    }

    @Test void isBitmap() {
        assertEquals(1651078259, FT_GLYPH_FORMAT_BITMAP);
    }

    @Test void testFontName() {
        try (FtLibrary library = new FtLibrary()) {
            String path = "../font/FreeSerif.ttf";

            FtFace face = library.newFace(path);
            long numFaces = face.getNumFaces();
            log.info("num faces:{}", numFaces);
            face.close();

            for (int i = 0; i < numFaces; i++) {
                face = library.newFace(path, i);
                log.info("face name: {}, style name:{}, post script:{}, format:{}", face.getFamilyName(), face.getStyleName(), face.getPostScriptName(), face.getFormat());
                face.close();
            }
        }
    }
    private String getFormat(int pixelMode) {
        Image.Format format;
        switch (pixelMode) {
            case FT_PIXEL_MODE_NONE:
                return "NONE";
            case FT_PIXEL_MODE_MONO:// 1bit per pixel, pitch = (width + 7)/ 8
                return "MONO";
            case FT_PIXEL_MODE_GRAY:
                format = Image.Format.Alpha8;
                break;
            case FT_PIXEL_MODE_GRAY2:
                format = Image.Format.Luminance8;
                break;
            default:
                return "Unknown";
        }
        return format.name();
    }
}
