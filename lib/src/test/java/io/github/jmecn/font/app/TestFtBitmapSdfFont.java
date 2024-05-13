package io.github.jmecn.font.app;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import io.github.jmecn.font.freetype.*;
import io.github.jmecn.font.utils.DebugPrintUtils;
import io.github.jmecn.font.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestFtBitmapSdfFont {

    public static void main(String[] args) {
        try (FtLibrary library = new FtLibrary()) {
            FtFace face = library.newFace("font/Noto_Serif_SC/NotoSerifSC-Regular.otf", 0);
            // face.setPixelSize(0, 16);
            face.setCharSize(0, FtLibrary.int26D6(16), 300, 300);

            String text = "你好世界";
            List<Image> imageList = new ArrayList<>();

            for (int i = 0; i < text.length(); i++) {
                int codepoint = Character.codePointAt(text, i);
                int glyphIndex = face.getCharIndex(codepoint);
                if (glyphIndex == 0) {
                    throw new UnsupportedOperationException("char not found:" + text);
                }
                // load glyph
                if (face.loadGlyph(glyphIndex, FT_LOAD_DEFAULT | FT_LOAD_NO_BITMAP)) {
                    // get glyph
                    FtGlyphSlot slot = face.getGlyph();
                    FtGlyphMetrics metrics = slot.getMetrics();
                    DebugPrintUtils.print(metrics);

                    // render glyph
                    FtGlyph glyph = slot.getGlyph();
                    FtBitmapGlyph bitmapGlyph = glyph.toBitmap(FT_RENDER_MODE_SDF);
                    FtBitmap bitmap = bitmapGlyph.getBitmap();
                    DebugPrintUtils.print(bitmap);
                    Image image = ImageUtils.ftBitmapToImage(bitmap, ColorRGBA.White, 1f);
                    imageList.add(image);
                }
            }

            // TestDisplay.run(Materials.UNSHADED, image);
            TestDisplay.run("Shaders/Font/SdFont.j3md", imageList.toArray(new Image[0]));
        }
    }
}
