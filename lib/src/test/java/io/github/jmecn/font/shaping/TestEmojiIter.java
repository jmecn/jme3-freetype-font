package io.github.jmecn.font.shaping;

import io.github.jmecn.text.EmojiIterator;
import io.github.jmecn.text.Unichar;
import org.junit.jupiter.api.Test;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestEmojiIter {

    void process(String text) {
        char[] chars = text.toCharArray();
        EmojiIterator iter = new EmojiIterator(chars);

        Unichar[] unichars = iter.getUnicodeChars();
        for (Unichar unichar : unichars) {
            System.out.println(unichar);
        }
        System.out.println("Unicode count:" + unichars.length);
        System.out.println(text);
        System.out.println("Character count:" + chars.length);
        while (iter.next()) {
            int start = iter.getStart();
            int end = iter.getEnd();
            int ts = iter.getTextStart();
            int te = iter.getTextEnd();
            String substr = text.substring(ts, te);
            System.out.printf("isEmoji:%b, unicode:[%d, %d), text:[%d, %s), %s\n", iter.isEmoji(), start, end, ts, te, substr);
        }
    }
    @Test void testSentence() {
        String text = "Hello" + "🙋🧑🧑🏻🧑🏼🧑🏽🧑🏾🧑🏿" + "world" + "🍰🐒" + "一家人" + "👨‍👩‍👧‍👦";
        process(text);
    }

    @Test void testZwjSequenceWithText() {
        String text = "我" + "👨‍👩‍👧‍👦";
        process(text);
    }

    @Test void testZwjSequence() {
        String text = "👨‍👩‍👧‍👦";
        process(text);
    }
}
