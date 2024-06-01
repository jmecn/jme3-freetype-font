package io.github.jmecn.text;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.lang.UScriptRun;
import org.junit.jupiter.api.Test;

import java.text.Bidi;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestBidiRun {
    static final String TEXT = "Love and peace." +// latin
            "爱与和平。世界是我们的，也是你们的。" +// Han
            "الحب 123والسلام" + // Arabic
            "사랑과 평화" + // Hangul
            "👋🤔️" // emoji
            ;

    @Test
    void testPropertyDetect() {
        for (int i = 0; i < TEXT.length(); i++) {
            int codepoint = Character.codePointAt(TEXT, i);
            byte dir = Character.getDirectionality(codepoint);
            Character.UnicodeScript script = Character.UnicodeScript.of(codepoint);

            System.out.printf("[U+%04X] %s %s %s, %s\n", codepoint, Character.getName(codepoint), Character.getType(codepoint), dir, script);
        }
    }

    @Test void testLineBreak() {
        BreakIterator iterator = BreakIterator.getLineInstance();
        iterator.setText(TEXT);
        // 迭代并分割文本
        int start = iterator.first();
        int end;
        while ((end = iterator.next()) != BreakIterator.DONE) {
            String line = TEXT.substring(start, end);
            System.out.println(line);
            start = end;
        }
    }
    @Test void testSimpleBidi() {
        List<BidiRun> bidiRuns = extractBidiRuns(TEXT);

        // 输出每个 BidiRun 的文本和方向性
        for (BidiRun bidiRun : bidiRuns) {
            System.out.printf("Directionality: %d %s\n", bidiRun.getDirectionality(), bidiRun.getText());
        }
    }

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

    // 将字符串分解为多个 BidiRun
    private static List<BidiRun> extractBidiRuns(String text) {
        List<BidiRun> bidiRuns = new ArrayList<>();
        StringBuilder runText = new StringBuilder();
        byte currentDirectionality = -1; // 初始方向性为 -1，表示未知

        // 遍历字符串中的每个字符
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            byte directionality = Character.getDirectionality(c);

            // 如果当前字符的方向性与前一个字符不同，或者当前字符是控制字符，则结束当前 Run，并添加到列表中
            if (directionality != currentDirectionality || Character.isMirrored(c)) {
                if (runText.length() > 0) {
                    bidiRuns.add(new BidiRun(runText.toString(), currentDirectionality));
                    runText.setLength(0);
                }
                currentDirectionality = directionality;
            }

            // 将当前字符添加到当前 Run 中
            runText.append(c);
        }

        // 添加最后一个 Run
        if (runText.length() > 0) {
            bidiRuns.add(new BidiRun(runText.toString(), currentDirectionality));
        }

        return bidiRuns;
    }

    // 表示一个 Bidi Run 的类
    static class BidiRun {
        private final String text;
        private final byte directionality;

        public BidiRun(String text, byte directionality) {
            this.text = text;
            this.directionality = directionality;
        }

        public String getText() {
            return text;
        }

        public byte getDirectionality() {
            return directionality;
        }
    }
}
