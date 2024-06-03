package io.github.jmecn.text;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestEmojiIterator {

    void test(String text, EmojiRun[] expectedList) {
        display(text);

        List<EmojiRun> actualList = new ArrayList<>();
        EmojiIterator iterator = new EmojiIterator(text.toCharArray());
        while (iterator.next()) {
            actualList.add(new EmojiRun(iterator.isEmoji(), iterator.getStart(), iterator.getEnd(), iterator.getTextStart(), iterator.getTextEnd()));
        }

        assertEquals(expectedList.length, actualList.size(), "size:" + text);
        int size = expectedList.length;
        for (int i = 0; i < size; i++) {
            EmojiRun expected = expectedList[i];
            EmojiRun actual = actualList.get(i);
            assertEquals(expected.isEmoji(), actual.isEmoji(), "isEmoji:" + text);
            assertEquals(expected.getUnicodeStart(), actual.getUnicodeStart(), "unicodeStart:" + text);
            assertEquals(expected.getUnicodeEnd(), actual.getUnicodeEnd(), "unicodeEnd:" + text);
            assertEquals(expected.getTextStart(), actual.getTextStart(), "textStart:" + text);
            assertEquals(expected.getTextEnd(), actual.getTextEnd(), "textEnd:" + text);
        }
    }

    void display(String text) {
        char[] chars = text.toCharArray();
        EmojiIterator iter = new EmojiIterator(chars);
        Unichar[] unichars = iter.getUnicodeChars();
        System.out.printf(">>>> %s <<<<\nunicode count:%d, character count:%d\n", text, unichars.length, chars.length);
        System.out.println("[id]:  unicode,   string, emoji, text");
        int runs = 0;

        while (iter.next()) {
            int start = iter.getStart();
            int end = iter.getEnd();
            int ts = iter.getTextStart();
            int te = iter.getTextEnd();
            String substr = text.substring(ts, te);
            System.out.printf("[%2d]: [%2d, %2d), [%2d, %2d), %5b, %s\n", runs++, start, end, ts, te, iter.isEmoji(), substr);
        }
        System.out.println();
    }

    static class TestData {
        String text;
        EmojiRun[] expectedList;

        TestData(String text, EmojiRun[] expectedList) {
            this.text = text;
            this.expectedList = expectedList;
        }
    }

    static List<TestData> getTestData() {
        String text;
        EmojiRun[] expectedList;

        List<TestData> list = new ArrayList<>();

        // not emoji
        text = "abc";
        expectedList = new EmojiRun[] {
                new EmojiRun(false, 0, 3, 0, 3)
        };
        list.add(new TestData(text, expectedList));

        // emoji base: smile
        text = "\uD83D\uDE0A";// 😊
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 1, 0, 2)
        };
        list.add(new TestData(text, expectedList));

        // zero-width joiner
        // family: man and woman and girl and boy
        text = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66";// 👨‍👩‍👧‍👦
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 7, 0, 11)
        };
        list.add(new TestData(text, expectedList));

        // emoji fitzpatrick modifier
        // a hand with light skin tone
        text = "\u270B\uD83C\uDFFB"; // ✋🏻
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 2, 0, 3)
        };
        list.add(new TestData(text, expectedList));

        // emoji fitzpatrick modifier and zero-width joiner
        // a female firefighter with medium-darker skin tone
        text = "\uD83D\uDC69\uD83C\uDFFD\u200D\uD83D\uDE92"; // 👩🏽‍🚒
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 4, 0, 7)
        };
        list.add(new TestData(text, expectedList));

        // alphanum: cool button
        text = "\uD83C\uDD92";// 🆒
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 1, 0, 2)
        };
        list.add(new TestData(text, expectedList));

        // flag: China
        text = "\uD83C\uDDE8\uD83C\uDDF3"; // 🇨🇳
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 2, 0, 4)
        };
        list.add(new TestData(text, expectedList));

        // flag: pirate flag
        text = "\uD83C\uDFF4\u200D\u2620\uFE0F";// 🏴‍☠️
        expectedList = new EmojiRun[] {
                new EmojiRun(true, 0, 4, 0, 5)
        };
        list.add(new TestData(text, expectedList));

        // keycap: #️⃣*️⃣0️⃣1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣
        text = "#\uFE0F\u20E3*\uFE0F\u20E30\uFE0F\u20E31\uFE0F\u20E32\uFE0F\u20E33\uFE0F\u20E34\uFE0F\u20E35\uFE0F\u20E36\uFE0F\u20E37\uFE0F\u20E38\uFE0F\u20E39\uFE0F\u20E3";
        expectedList = new EmojiRun[]{
                new EmojiRun(true, 0, 36, 0, 36),
        };
        list.add(new TestData(text, expectedList));

        // complex text
        text = "Hello, 你好，🌍世界！";
        expectedList = new EmojiRun[] {
                new EmojiRun(false, 0, 10, 0, 10),
                new EmojiRun(true, 10, 11, 10, 12),
                new EmojiRun(false, 11, 14, 12, 15),
        };
        list.add(new TestData(text, expectedList));

        // complex emoji combined with text
        text = "Hello" + "🙋🧑🧑🏻🧑🏼🧑🏽🧑🏾🧑🏿" + "world" + "🍰🐒" + "家庭" + "👨‍👩‍👧‍👦";
        expectedList = new EmojiRun[] {
                new EmojiRun(false, 0, 5, 0, 5),
                new EmojiRun(true, 5, 17, 5, 29),
                new EmojiRun(false, 17, 22, 29, 34),
                new EmojiRun(true, 22, 24, 34, 38),
                new EmojiRun(false, 24, 26, 38, 40),
                new EmojiRun(true, 26, 33, 40, 51)
        };

        list.add(new TestData(text, expectedList));

        return list;
    }

    @Test void testAll() {
        for (TestData data : getTestData()) {
            test(data.text, data.expectedList);
        }
    }
}
