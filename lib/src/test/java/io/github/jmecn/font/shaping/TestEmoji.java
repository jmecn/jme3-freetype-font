package io.github.jmecn.font.shaping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestEmoji {

    @Test void charDetect() {
        String text = "Hello😊";

        // the string looks like only have 6 chars, but emoji is a surrogate pair, so the length is 7
        assertFalse(6 == text.length());
        assertTrue(7 == text.length());

        // the 6th and 7th char is a surrogate pair
        assertTrue(Character.isHighSurrogate(text.charAt(5)));
        assertTrue(Character.isLowSurrogate(text.charAt(6)));

        // the codepoint is not equal to the char
        assertEquals(0xD83D, text.charAt(5));
        assertEquals(0x1F60A, Character.codePointAt(text, 5));

        // 0xDE0A is a control character, it is not a high surrogate
        assertEquals(0xDE0A, text.charAt(6));
        assertEquals(0xDE0A, Character.codePointAt(text, 6));

        // print all chars
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int codepoint = Character.codePointAt(text, i);
            System.out.printf("char=%c, charAt=0x%X, codepoint=0x%X isHighSurrogate=%b, isLowSurrogate=%b\n", c, (int)c, codepoint, Character.isHighSurrogate(c), Character.isLowSurrogate(c));
        }
    }

    @Test void testFitzpatrickModifier() {
        String text = "\uD83E\uDDD1\uD83E\uDDD1\uD83C\uDFFB\uD83E\uDDD1\uD83C\uDFFC\uD83E\uDDD1\uD83C\uDFFD\uD83E\uDDD1\uD83C\uDFFE\uD83E\uDDD1\uD83C\uDFFF";
        System.out.println(text);
        System.out.println("\uD83C\uDFFB");
        System.out.println("\uD83C\uDFFC");
        System.out.println("\uD83C\uDFFD");
        System.out.println("\uD83C\uDFFE");
        System.out.println("\uD83C\uDFFF");

        // print all chars
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int codepoint = Character.codePointAt(text, i);
            System.out.printf("char=%c, charAt=0x%X, codepoint=0x%X isHighSurrogate=%b, isLowSurrogate=%b\n", c, (int)c, codepoint, Character.isHighSurrogate(c), Character.isLowSurrogate(c));
        }
    }

    @Test void testEmojiZwj() {
        String name = "👨‍👩‍👧‍👦";
        assertEquals(11, name.length());
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66", name);
        System.out.println("\uD83D\uDC68");
        System.out.println("\uD83D\uDC69");
        System.out.println("\uD83D\uDC67");
        System.out.println("\uD83D\uDC66");
    }
}
