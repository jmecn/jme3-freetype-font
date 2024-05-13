package io.github.jmecn.font.app;

import com.jme3.texture.Image;
import io.github.jmecn.font.generator.FtBitmapFontData;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.Glyph;
import io.github.jmecn.font.packer.PackerPage;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestGenerateImage {

    static final String FONT = "font/Noto_Serif_SC/NotoSerifSC-Regular.otf";
    public static void main(String[] args) throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT), 0)) {
            FtFontParameter parameter = new FtFontParameter();
            generator.generate(parameter);
            // show image
            Image[] images = parameter.getPacker().getPages().stream().map(PackerPage::getImage).toArray(Image[]::new);
            TestDisplay.run(null, images);
        }
    }
}
