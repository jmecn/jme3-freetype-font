package io.github.jmecn.font.shaping;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.lang.UScriptRun;
import org.junit.jupiter.api.Test;

import java.text.Bidi;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/21
 */
public class TestCharDetect {
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

    private static final Pattern tagPattern = Pattern.compile("<(b|i|u|color|span|style)(.*?)>(.*?)</\\1>");

    public static void parse(String gmarkText) {
        Matcher matcher = tagPattern.matcher(gmarkText);
        while (matcher.find()) {
            String tag = matcher.group(1);
            String attributes = matcher.group(2);
            String content = matcher.group(3);
            System.out.println("Tag: " + tag);
            if (!attributes.isEmpty()) {
                System.out.println("Attributes: " + attributes);
            }
            System.out.println("Content: " + content);
        }
    }

    @Test void testParseMarker() {
        String gmarkText = "<b>Hello</b> <i>world</i> <color id=\"#FFCCDD\">!</color>";
        parse(gmarkText);
    }

    public static List<TextSpan> extractTagContents(TextSpan parentSpan) {
        List<TextSpan> contents = new ArrayList<>();
        Matcher matcher = tagPattern.matcher(parentSpan.text);
        int lastEnd = 0;
        while (matcher.find()) {
            // 添加标签之前的文本部分
            String beforeTag = parentSpan.text.substring(lastEnd, matcher.start());
            if (!beforeTag.isEmpty()) {
                contents.add(new TextSpan(beforeTag, parentSpan.attributes));
            }
            String tag = matcher.group(1);
            String attributes = matcher.group(2);
            // 添加标签内的内容
            String content = matcher.group(3);

            List<String> attrList = new ArrayList<>();
            if (parentSpan.attributes != null) {
                // 外层优先级低，放在前面
                attrList.addAll(parentSpan.attributes);
            }
            // 内层优先级高，放在后面。
            attrList.add(tag + ":" + attributes);
            if (!content.isEmpty()) {
                TextSpan span = new TextSpan(content, attrList);
                if (tagPattern.matcher(content).find()) {
                    List<TextSpan> spans = extractTagContents(span);
                    contents.addAll(spans);
                } else {
                    contents.add(span);
                }
            }
            // 更新上一个标签结束的位置
            lastEnd = matcher.end();
        }
        // 添加剩余的文本部分
        String remainder = parentSpan.text.substring(lastEnd);
        if (!remainder.isEmpty()) {
            contents.add(new TextSpan(remainder, parentSpan.attributes));
        }
        return contents;
    }

    public static List<TextSpan> extractTagContents(String gmarkText) {
        List<TextSpan> contents = new ArrayList<>();
        Matcher matcher = tagPattern.matcher(gmarkText);
        int lastEnd = 0;
        while (matcher.find()) {
            // 添加标签之前的文本部分
            String beforeTag = gmarkText.substring(lastEnd, matcher.start());
            if (!beforeTag.isEmpty()) {
                contents.add(new TextSpan(beforeTag, null));
            }
            String tag = matcher.group(1);
            String attributes = matcher.group(2);
            // 添加标签内的内容
            String content = matcher.group(3);

            List<String> attrList = new ArrayList<>();
            attrList.add(tag + ":" + attributes);
            if (!content.isEmpty()) {
                TextSpan span = new TextSpan(content, attrList);
                if (tagPattern.matcher(content).find()) {
                    List<TextSpan> spans = extractTagContents(span);
                    contents.addAll(spans);
                } else {
                    contents.add(span);
                }
            }
            // 更新上一个标签结束的位置
            lastEnd = matcher.end();
        }
        // 添加剩余的文本部分
        String remainder = gmarkText.substring(lastEnd);
        if (!remainder.isEmpty()) {
            contents.add(new TextSpan(remainder, null));
        }
        return contents;
    }

    @Test void parseTag() {
        String gmarkText = "This is a nice place. <style id='my-style'><i><b>Hello</b>,<span color='#FF0000'>world</span></i>I hope <u>you</u> happy here.</style>";
        List<TextSpan> extractedContents = extractTagContents(gmarkText);
        for (TextSpan content : extractedContents) {
            System.out.println(content);
        }
    }

    static class TextSpan {
        String text;
        List<String> attributes;

        public TextSpan(String text, List<String> attributes) {
            this.text = text;
            this.attributes = attributes;
        }

        public void add(List<String> attributes) {
            if (this.attributes == null) {
                this.attributes = new ArrayList<>();
            }
            this.attributes.addAll(attributes);
        }
        @Override
        public String toString() {
            return "TextSpan{" +
                    "text='" + text + '\'' +
                    ", attributes=" + attributes +
                    '}';
        }
    }
}
