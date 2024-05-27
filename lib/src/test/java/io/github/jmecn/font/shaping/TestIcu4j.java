package io.github.jmecn.font.shaping;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.lang.UScriptRun;
import com.ibm.icu.text.Bidi;
import org.junit.jupiter.api.Test;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestIcu4j {

    static final String TEXT = "Love and peace" +// latin
            "爱与和平" +// Han
            "الحب والسلام" + // Arabic
            "사랑과 평화" // Hangul
    ;

    @Test void testUScriptRun() {
        UScriptRun run = new UScriptRun(TEXT);
        while (run.next()) {
            int start = run.getScriptStart();
            int limit = run.getScriptLimit();
            int script = run.getScriptCode();
            System.out.printf("Script %s from %d to %d\n", UScript.getName(script), start, limit);
        }
    }

    @Test void testBidi() {
        Bidi bidi = new Bidi(TEXT, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        System.out.printf("isMixed:%b, runCount:%d\n", bidi.isMixed(), bidi.getRunCount());

        for (int i = 0; i < bidi.getRunCount(); i++) {
            int start = bidi.getRunStart(i);
            int limit = bidi.getRunLimit(i);
            System.out.printf("start=%d, limit=%d, level=%d\n", start, limit, bidi.getRunLevel(i));// 0-left_to_right, 1-right_to_left
        }
    }

}
