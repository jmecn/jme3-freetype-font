package io.github.jmecn.font.shaping;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.lang.UScriptRun;
import org.junit.jupiter.api.Test;

import java.text.Bidi;
import java.text.BreakIterator;
import java.util.Arrays;

import static org.lwjgl.util.harfbuzz.HarfBuzz.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestIcu4j {

    static final String TEXT = "Love and peace" +// latin
            "爱与和平" +// Han
            "الحب 123والسلام" + // Arabic
            "사랑과 평화" + // Hangul
            "👋🤔️" // emoji
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
            System.out.printf("start=%d, limit=%d, level=%d, %s\n", start, limit, bidi.getRunLevel(i), TEXT.substring(start, limit));// 0-left_to_right, 1-right_to_left
        }
    }

    @Test void testBreakIterator() {
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(TEXT);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            System.out.println(start + ": " + TEXT.substring(start, end));
        }
    }

    @Test void testTokenize() {
        int length = TEXT.length();
        int[] data = new int[length];

        // detect bidi first
        Bidi bidi = new Bidi(TEXT, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        int runCount = bidi.getRunCount();
        for (int i = 0; i < runCount; i++) {
            int start = bidi.getRunStart(i);
            int limit = bidi.getRunLimit(i);
            int level = bidi.getRunLevel(i);
            for (int j = start; j < limit; j++) {
                data[j] |= level << 8;
            }
        }

        // detect unicode script then
        UScriptRun scriptRun = new UScriptRun(TEXT);
        while (scriptRun.next()) {
            int start = scriptRun.getScriptStart();
            int limit = scriptRun.getScriptLimit();
            int script = scriptRun.getScriptCode();
            for (int i = start; i < limit; i++) {
                data[i] |= script;
            }
        }
        System.out.println(Arrays.toString(data));

        // detect emoji then
        // TODO
    }

    @Test void testHarfbuzzScript() {
        // use harfbuzz to do the same
        // icu4j is not needed now
        long hb_unicode_funcs = hb_unicode_funcs_create(hb_unicode_funcs_get_default());

        int length = TEXT.length();
        for (int i = 0; i < length; i++) {
            int codepoint = TEXT.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codepoint)) {
                i++;
            }
            int script = hb_unicode_script(hb_unicode_funcs, codepoint);
            int direction = hb_script_get_horizontal_direction(script);

            // tag to string, for display purpose
            int c1 = script >> 24;
            int c2 = (script >> 16) & 0xFF;
            int c3 = (script >> 8) & 0xFF;
            int c4 = script & 0xFF;
            System.out.printf("%s, codepoint=U+%X, direction=%d, script=%X, tag=%c%c%c%c\n", (char)codepoint, codepoint, direction, script, c1, c2, c3, c4);
        }

        hb_unicode_funcs_destroy(hb_unicode_funcs);
    }
}
