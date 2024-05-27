package io.github.jmecn.font.shaping;

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
import static org.lwjgl.util.harfbuzz.OpenType.*;

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
        FtFace face = library.newFace("../font/NotoSerifSC-Regular.otf");
        face.setPixelSize(0, 16);

        long hb_face_t;
        long hb_ft_font;

        hb_face_t = hb_ft_face_create_referenced(face.address());
        hb_ft_font = hb_font_create (hb_face_t);

        hb_ft_font_set_funcs(hb_ft_font);
        hb_font_set_scale(hb_ft_font, FONT_SIZE << 6, FONT_SIZE << 6);

        // Create  HarfBuzz buffer
        long buf = hb_buffer_create();
        /* Call the setup_buffer first while the buffer is empty,
         * as guess_segment_properties doesn't like glyphs in the buffer. */

        // Set buffer to LTR direction, common script and default language
        hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
        hb_buffer_set_script(buf, HB_SCRIPT_COMMON);
        hb_buffer_set_language(buf, hb_language_from_string("zh"));

        // Add text and layout it
        hb_buffer_add_utf8(buf, TEXT, 0, -1);
        hb_shape(hb_ft_font, buf, null);

        // Get buffer data
        int glyph_count = hb_buffer_get_length (buf);
        hb_glyph_info_t.Buffer     glyph_info   = hb_buffer_get_glyph_infos(buf);
        hb_glyph_position_t.Buffer glyph_pos    = hb_buffer_get_glyph_positions(buf);

        System.out.printf("glyph_count=%d\n", glyph_count);

        int string_width_in_pixels = 0;
        for (int i = 0; i < glyph_count; ++i) {
            int codepoint = glyph_info.get(i).codepoint();
            int x_advance = glyph_pos.get(i).x_advance() >> 6;
            int glyphIndex = face.getCharIndex(codepoint);
            string_width_in_pixels += x_advance;
            System.out.printf("codepoint=0x%X, x_advance=%d, glyphIndex=0x%X\n", codepoint, x_advance, glyphIndex);
        }

        System.out.printf("string_width=%d \n", string_width_in_pixels);

        hb_buffer_destroy(buf);
        hb_font_destroy(hb_ft_font);
        hb_face_destroy(hb_face_t);

        face.close();
        library.close();
    }

    @Test void testFallback() {
        // init lwjgl3 harfbuzz with freetype
        Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());

        // load 2 fonts, english first and chinese second
        FtLibrary library = new FtLibrary();

        String[] fonts = new String[] {
                "../font/FreeSerif.ttf",
                "../font/NotoSerifSC-Regular.otf",
                "../font/NotoColorEmoji-Regular.ttf"
        };
        int fontCount = fonts.length;

        // load all fonts
        FtFace[] faces = new FtFace[fontCount];
        long[] hb_face_t = new long[fontCount];
        long[] hb_font_t = new long[fontCount];

        for (int i = 0; i < fontCount; i++) {
            faces[i] = library.newFace(fonts[i]);
            faces[i].selectBestPixelSize(32);

            hb_face_t[i] = hb_ft_face_create_referenced(faces[i].address());
            hb_font_t[i] = hb_font_create(hb_face_t[i]);
       }

        // 顺序加载字符串，按照字体顺序来查找 glyphIndex，如果找不到就记下其位置，在下一个字体中查找，如果找不到就报错。
        String text = "Hello, world! 你好，世界！👋👨‍👩‍👧‍👦";

        // 迭代次数，最大不超过字体的数量
        for (int iteration = 0; iteration < fonts.length; iteration++) {

            FtFace face = faces[iteration];
            hb_ft_font_set_funcs(hb_font_t[iteration]);// should I use hb_ft_font_set_funcs or hb_font_set_funcs?
            hb_font_set_scale(hb_font_t[iteration], FONT_SIZE << 6, FONT_SIZE << 6);

            // Create  HarfBuzz buffer
            long buf = hb_buffer_create();
            /* Call the setup_buffer first while the buffer is empty,
             * as guess_segment_properties doesn't like glyphs in the buffer. */

            hb_buffer_add_utf8(buf, text, 0, -1);
            hb_buffer_set_content_type(buf, HB_BUFFER_CONTENT_TYPE_UNICODE);
            // Set buffer to LTR direction, common script and default language
            //hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
            //hb_buffer_set_script(buf, HB_SCRIPT_COMMON);
            //hb_buffer_set_language(buf, hb_language_from_string("zh"));

            hb_shape(hb_font_t[0], buf, null);

            hb_glyph_info_t.Buffer glyph_info = hb_buffer_get_glyph_infos(buf);
            int glyph_count = hb_buffer_get_length(buf);
            System.out.println("glyph_count=" + glyph_count);
            for (int i = 0; i < glyph_count; ++i) {
                int codepoint = glyph_info.get(i).codepoint();
                int glyphIndex = face.getCharIndex(codepoint);
                System.out.printf("codepoint=0x%X, glyph_index=0x%X\n", codepoint, glyphIndex);
            }

            hb_buffer_destroy(buf);
        }

        //// release all resources
        for (int i = 0; i < fontCount; i++) {
            hb_font_destroy(hb_font_t[i]);
            hb_face_destroy(hb_face_t[i]);
            faces[i].close();
        }

        library.close();
    }

    @Test void testHarfbuzzEmojiZwj() {
        String text = "👨‍👩‍👧‍👦";
        // init lwjgl3 harfbuzz with freetype
        Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());

        // load 2 fonts, english first and chinese second
        FtLibrary library = new FtLibrary();

        String fonts = "../font/NotoColorEmoji-Regular.ttf";
        FtFace face = library.newFace(fonts, 0);
        face.setPixelSize(0, FONT_SIZE);
        face.selectCharmap(FreeType.FT_ENCODING_UNICODE);

        // loading from FT
        // For Harfbuzz, load using OpenType (HarfBuzz FT does not support bitmap font)
        long hb_blob_t = hb_blob_create_from_file(fonts);
        long hb_face_t = hb_face_create(hb_blob_t, 0);
        logger.info("hasSvg:{}, hasPng:{}", hb_ot_color_has_svg(hb_face_t), hb_ot_color_has_png(hb_face_t));

        hb_blob_destroy(hb_blob_t);
        long hb_font_t = hb_font_create(hb_face_t);
        hb_ot_font_set_funcs(hb_font_t);
        hb_font_set_scale(hb_font_t, FONT_SIZE << 6, FONT_SIZE << 6);

        // Create  HarfBuzz buffer
        long buf = hb_buffer_create();

        hb_buffer_add_utf8(buf, text, 0, -1);
        ///hb_buffer_set_content_type(buf, HB_BUFFER_CONTENT_TYPE_UNICODE);

        // Set buffer to LTR direction, common script and default language
        hb_buffer_set_direction(buf, HB_DIRECTION_LTR);
        hb_buffer_set_script(buf, HB_SCRIPT_LATIN);
        hb_buffer_set_language(buf, hb_language_from_string("en"));
        // hb_buffer_guess_segment_properties(buf);

        hb_shape(hb_font_t, buf, null);

        hb_glyph_info_t.Buffer glyph_info = hb_buffer_get_glyph_infos(buf);
        hb_glyph_position_t.Buffer glyph_position = hb_buffer_get_glyph_positions(buf);

        int glyph_count = hb_buffer_get_length(buf);
        System.out.println("glyph_count=" + glyph_count);
        for (int i = 0; i < glyph_count; ++i) {
            int codepoint = glyph_info.get(i).codepoint();
            glyph_position.get(i).x_advance();
            int glyphIndex = face.getCharIndex(codepoint);
            boolean load = face.loadGlyph(glyphIndex, FreeType.FT_LOAD_DEFAULT);
            System.out.printf("codepoint=0x%X, glyph_index=0x%X, load=%b\n", codepoint, glyphIndex, load);
        }

        hb_buffer_destroy(buf);
        hb_font_destroy(hb_font_t);
        hb_face_destroy(hb_face_t);

        face.close();

        library.close();
    }
}
