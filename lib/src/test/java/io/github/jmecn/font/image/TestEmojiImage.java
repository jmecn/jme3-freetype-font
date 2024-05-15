package io.github.jmecn.font.image;

import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import io.github.jmecn.font.freetype.*;
import io.github.jmecn.font.utils.DebugPrintUtils;
import io.github.jmecn.font.utils.ImageUtils;
import org.lwjgl.system.Configuration;
import org.lwjgl.util.freetype.FreeType;
import org.lwjgl.util.harfbuzz.hb_glyph_info_t;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.*;
import static org.lwjgl.util.harfbuzz.HarfBuzz.*;
import static org.lwjgl.util.harfbuzz.OpenType.hb_ot_font_set_funcs;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestEmojiImage {

    static final String APPLE_EMOJI = "/System/Library/Fonts/Apple Color Emoji.ttc";
    static final String TEXT = "🙋👋🍰🐒";

    public static void main(String[] args) {
        // init lwjgl3 harfbuzz with freetype
        Configuration.HARFBUZZ_LIBRARY_NAME.set(FreeType.getLibrary());

        List<Image> imageList = new ArrayList<>();

        try (FtLibrary library = new FtLibrary()) {
            FtFace face = library.newFace(APPLE_EMOJI, 0);
            // face.setPixelSize(0, 16);
            // face.setCharSize(0, FtLibrary.int26D6(32), 300, 300);

            System.out.printf("has emoji:%b\n", face.hasEmoji());
            face.selectBestPixelSize(64);

            long hb_face_t;
            long hb_ft_font;

            // For Harfbuzz, load using OpenType (HarfBuzz FT does not support bitmap font)
            hb_face_t = hb_ft_face_create_referenced(face.address());
            hb_ft_font = hb_font_create (hb_face_t);

            hb_ot_font_set_funcs(hb_ft_font);

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
            int glyph_count = hb_buffer_get_length (buf);
            hb_glyph_info_t.Buffer glyph_info = hb_buffer_get_glyph_infos(buf);
            System.out.printf("glyph_count:%d\n", glyph_count);

            for (int i = 0; i < glyph_count; i++) {
                int codepoint = glyph_info.get(i).codepoint();
                int glyphIndex = face.getCharIndex(codepoint);
                if (glyphIndex == 0) {
                    System.err.printf("char not found, codepoint:0x%X\n", codepoint);
                    continue;
                } else {
                    System.out.printf("char found, codepoint:0x%X, glyphIndex:%d\n", codepoint, glyphIndex);
                }
                // load glyph
                if (face.loadGlyph(glyphIndex, FT_LOAD_DEFAULT | FT_LOAD_COLOR)) {
                    // get glyph
                    FtGlyphSlot slot = face.getGlyph();
                    FtGlyphMetrics metrics = slot.getMetrics();
                    DebugPrintUtils.print(metrics);

                    // render glyph
                    FtGlyph glyph = slot.getGlyph();
                    FtBitmapGlyph bitmapGlyph = glyph.toBitmap(FT_RENDER_MODE_NORMAL);
                    FtBitmap bitmap = bitmapGlyph.getBitmap();
                    DebugPrintUtils.printBitmapInfo(bitmap);
                    if (bitmap.getBufferSize() == 0) {
                        System.out.println("size is empty");
                        continue;
                    }
                    Image image = ImageUtils.ftBitmapToImage(bitmap, ColorRGBA.White, 1.8f);
                    imageList.add(image);
                }
            }

            hb_buffer_destroy(buf);
            hb_font_destroy(hb_ft_font);
            hb_face_destroy(hb_face_t);
        }
        TestDisplay.run(Materials.UNSHADED, imageList.toArray(new Image[0]));
    }
}
