package io.github.jmecn.font;

import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestGenerator {
    static Logger logger = LoggerFactory.getLogger(TestGenerator.class);

    public static final String FONT = "../font/NotoSerifSC-Regular.otf";
    @Test void main() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT))) {
            FtFontParameter parameter = new FtFontParameter();
            generator.generateData(parameter);
        }
    }

    @Test void incremental() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT))) {
            FtFontParameter parameter = new FtFontParameter();
            parameter.setIncremental( true );
            FtBitmapCharacterSet data = generator.generateData(parameter);
            String str = "你好世界!";
            for( int i = 0; i < str.length(); i++) {
                Glyph glyph = data.getCharacter(str.charAt(i));
                logger.info("glyph:{}", glyph);
            }
        }
    }

    @Test void testGlyph() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File("../font/FreeSerif.ttf"))) {
            FtFontParameter parameter = new FtFontParameter();
            parameter.setSize(32);
            parameter.setPadding(1);
            parameter.setCharacters("ABCDEFG");
            FtBitmapCharacterSet data = generator.generateData(parameter);

            logger.info("data: width={}, height={}, lineHeight={}, base={}, ascent={}, descent:{}", data.getWidth(), data.getHeight(), data.getLineHeight(), data.getBase(), data.getAscent(), data.getDescent());
            for (Glyph glyph : data.getGlyphs()) {
                logger.info("glyph:{}", glyph);
            }

        }
    }
}
