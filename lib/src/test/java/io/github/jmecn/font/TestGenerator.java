package io.github.jmecn.font;

import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/12
 */
public class TestGenerator {
    public static final String FONT = "../font/Noto_Serif_SC/NotoSerifSC-Regular.otf";
    @Test void main() throws Exception {
        try (FtFontGenerator generator = new FtFontGenerator(new File(FONT), 0)) {
            FtFontParameter parameter = new FtFontParameter();
            generator.generate(parameter);
        }
    }
}
