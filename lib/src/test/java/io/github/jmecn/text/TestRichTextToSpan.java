package io.github.jmecn.text;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test case to split rich text into text spans.
 *
 * @author yanmaoyuan
 */
public class TestRichTextToSpan {

    private static final Pattern tagPattern = Pattern.compile("<(b|i|u|color|span|style)(.*?)>(.*?)</\\1>");

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
