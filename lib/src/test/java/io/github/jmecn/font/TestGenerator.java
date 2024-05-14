package io.github.jmecn.font;

import io.github.jmecn.font.generator.FtBitmapFontData;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.Glyph;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/12
 */
public class TestGenerator {
    static Logger logger = LoggerFactory.getLogger(TestGenerator.class);

    public static final String FONT = "../font/Noto_Serif_SC/NotoSerifSC-Regular.otf";
    @Test void main() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT), 0)) {
            FtFontParameter parameter = new FtFontParameter();
            generator.generateData(parameter);
        }
    }

    @Test void incremental() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT), 0)) {
            FtFontParameter parameter = new FtFontParameter();
            parameter.incremental = true;
            FtBitmapFontData data = generator.generateData(parameter);
            String str = "你好世界!";
            for( int i = 0; i < str.length(); i++) {
                Glyph glyph = data.getGlyph(str.charAt(i));
                logger.info("glyph:{}", glyph);
            }
        }
    }
}
