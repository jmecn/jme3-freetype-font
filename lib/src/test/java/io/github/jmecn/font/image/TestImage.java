package io.github.jmecn.font.image;

import com.jme3.texture.Image;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.Glyph;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestImage {
    public static final String FONT = "font/Noto_Serif_SC/NotoSerifSC-Regular.otf";

    static Logger logger = LoggerFactory.getLogger(TestImage.class);
    public static void main(String[] args) throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT))) {
            FtFontParameter parameter = new FtFontParameter();
            parameter.setIncremental(true);
            FtBitmapCharacterSet data = generator.generateData(parameter);
            String str = "你好世界!";
            for( int i = 0; i < str.length(); i++) {
                Glyph glyph = data.getCharacter(str.charAt(i));
                logger.info("glyph:{}", glyph);
            }

            Packer packer = parameter.getPacker();
            List<Page> pages = packer.getPages();
            Image[] images = pages.stream().map(Page::getImage).toArray(Image[]::new);
            TestDisplay.run(null, images);
        }
    }
}
