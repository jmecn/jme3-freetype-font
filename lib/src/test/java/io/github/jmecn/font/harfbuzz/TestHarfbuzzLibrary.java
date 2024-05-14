package io.github.jmecn.font.harfbuzz;

import io.github.jmecn.font.freetype.FtFace;
import io.github.jmecn.font.freetype.FtLibrary;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.Configuration;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.harfbuzz.hb_glyph_info_t;
import org.lwjgl.util.harfbuzz.hb_glyph_position_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.util.harfbuzz.HarfBuzz.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestHarfbuzzLibrary {

    static Logger logger = LoggerFactory.getLogger(TestHarfbuzzLibrary.class);

    static final int FONT_SIZE = 32;
    static final String TEXT = "你 好👋";

    @Test void testStringChar() {
        int len = TEXT.length();
        System.out.printf("glyph_count=%d\n", len);
        for (int i = 0; i < len; i++) {
            int codepoint = Character.codePointAt(TEXT, i);
            System.out.printf("codepoint=0x%X\n", codepoint);
        }
    }

    /**
     * <h3>FreeType interop</h3>
     *
     * <p>The default LWJGL HarfBuzz build does not include FreeType support and the {@code hb_ft_*} functions will not be available. However, LWJGL's FreeType
     * build includes HarfBuzz and exports its full API. When working with both HarfBuzz and FreeType, the HarfBuzz bindings can be made to use FreeType's
     * shared library, with one of the following ways:</p>
     *
     * <ul>
     * <li>launch the JVM with {@code -Dorg.lwjgl.harfbuzz.libname=freetype}</li>
     * <li>run {@code Configuration.HARFBUZZ_LIBRARY_NAME.set("freetype")}</li>
     * <li>run {@code Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary())} - recommended</li>
     * </ul>
     *
     * <p>The {@code org.lwjgl.harfbuzz.natives} module is not necessary when enabling the above.</p>
     */
    @Test void testHarfbuzzWithFreeType() {
        // init lwjgl3 harfbuzz with freetype
        Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());

        FtLibrary library = new FtLibrary();
        FtFace face = library.newFace("../font/Noto_Serif_SC/NotoSerifSC-Regular.otf");
        face.setPixelSize(0, 16);

        long hb_face_t;
        long hb_ft_font;

        hb_face_t = hb_ft_face_create_referenced(face.address());
        hb_ft_font = hb_font_create (hb_face_t);

        hb_ft_font_set_funcs(hb_ft_font);
        hb_font_set_scale(hb_ft_font, FONT_SIZE << 6, FONT_SIZE << 6);

        // Create  HarfBuzz buffer
        long buf = hb_buffer_create();

        // Set buffer to LTR direction, common script and default language
        // hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
        // hb_buffer_set_script(buf, HB_SCRIPT_COMMON);
        // hb_buffer_set_language(buf, hb_language_get_default());

        // Add text and layout it
        hb_buffer_add_utf8(buf, TEXT, 0, -1);
        hb_shape(hb_ft_font, buf, null);

        // Get buffer data
        int        glyph_count = hb_buffer_get_length (buf);
        hb_glyph_info_t.Buffer     glyph_info   = hb_buffer_get_glyph_infos(buf);
        hb_glyph_position_t.Buffer glyph_pos    = hb_buffer_get_glyph_positions(buf);

        System.out.printf("glyph_count=%d\n", glyph_count);

        int string_width_in_pixels = 0;
        for (int i = 0; i < glyph_count; ++i) {
            int codepoint = glyph_info.get(i).codepoint();
            int x_advance = glyph_pos.get(i).x_advance() >> 6;
            string_width_in_pixels += x_advance;
            System.out.printf("codepoint=0x%X, x_advance=%d\n", codepoint, x_advance);
        }

        System.out.printf("string_width=%d \n", string_width_in_pixels);

        hb_buffer_destroy(buf);
        hb_font_destroy(hb_ft_font);
        hb_face_destroy(hb_face_t);

        face.close();
        library.close();
    }
}
