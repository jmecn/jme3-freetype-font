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

    public static final String FONT = "../font/Noto_Serif_SC/NotoSerifSC-Regular.otf";
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
}
