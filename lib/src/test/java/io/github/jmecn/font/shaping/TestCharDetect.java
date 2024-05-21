package io.github.jmecn.font.shaping;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.ibm.icu.text.UnicodeSet;
import org.junit.jupiter.api.Test;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/21
 */
public class TestCharDetect {

    @Test
    void testCharsetDetect() {
        String text = "Hello, world! 你好，世界！";
        CharsetDetector detector = new CharsetDetector();
        detector.setText(text.getBytes());
        for (CharsetMatch match : detector.detectAll()) {
            System.out.printf("%s, %d, %s\n", match.getName(), match.getConfidence(), match.getLanguage());
        }
    }

    @Test void testUnicodeSet() {
        UnicodeSet set = new UnicodeSet();
    }
}
