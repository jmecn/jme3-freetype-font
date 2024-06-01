package io.github.jmecn.text;

import static io.github.jmecn.text.EmojiCategory.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class EmojiIterator {

    int text_start;
    int text_end;
    int start;
    int end;
    int nChars;
    boolean isEmoji;

    Unichar[] unichars;
    byte[] types;
    int cursor;

    public EmojiIterator(char[] text) {
        types = new byte[text.length];
        Unichar[] chars = new Unichar[text.length];
        nChars = 0;
        int i = 0;
        while (i < text.length) {
            int codepoint = Character.codePointAt(text, i);
            byte type = emojiSegmentationCategory(codepoint);
            types[nChars] = type;

            Unichar ch = new Unichar();
            ch.codepoint = codepoint;
            ch.emojiType = type;
            ch.start = i;
            i+= Character.charCount(codepoint);
            ch.end = i;
            chars[nChars] = ch;
            nChars++;
        }
        if (nChars == text.length) {
            this.unichars = chars;
        } else {
            this.unichars = new Unichar[nChars];
            System.arraycopy(chars, 0, this.unichars, 0, nChars);
        }

        text_start = 0;
        start = 0;
        end = 0;

        text_end = nChars - 1;

        isEmoji = false;
        cursor = 0;
    }

    public boolean isEmoji() {
        return isEmoji;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getTextStart() {
        return unichars[start].start;
    }

    public int getTextEnd() {
        return unichars[end-1].end;
    }

    public boolean next() {
        int pointer;

        if (end >= text_end) {
            return false;
        }

        start = end;

        pointer = cursor;

        EmojiIteratorResult sr = EmojiPresentationScanner.scan_emoji_presentation(types, pointer, nChars);
        pointer = sr.end;
        boolean flag = sr.isEmoji;

        do {
            cursor = pointer;
            this.isEmoji = flag;

            if (pointer == nChars) {// end
                break;
            }

            sr = EmojiPresentationScanner.scan_emoji_presentation(types, pointer, nChars);
            pointer = sr.end;
            flag = sr.isEmoji;
        } while (this.isEmoji == flag);

        end = cursor;

        return true;
    }

    private static boolean binSearch(int c, int[][] table) {
        int lower = 0;
        int upper = table.length - 1;
        while (lower <= upper) {
            int mid = (lower + upper) / 2;
            if (c < table[mid][0])
                upper = mid - 1;
            else if (c > table[mid][1])
                lower = mid + 1;
            else
                return true;
        }
        return false;
    }

    public static boolean isEmoji(int codepoint) {
        int[][] table = EmojiTable.EMOJI_TABLE;
        return codepoint >= table[0][0] && binSearch(codepoint, table);
    }

    public static boolean isEmojiModifier(int codepoint) {
        int[][] table = EmojiTable.EMOJI_MODIFIER_TABLE;
        return (codepoint >= table[0][0] && binSearch(codepoint, table));
    }

    public static boolean isEmojiModifierBase(int codepoint) {
        int[][] table = EmojiTable.EMOJI_MODIFIER_BASE_TABLE;
        return (codepoint >= table[0][0] && binSearch(codepoint, table));
    }

    public static boolean isEmojiPresentation(int codepoint) {
        int[][] table = EmojiTable.EMOJI_PRESENTATION_TABLE;
        return (codepoint >= table[0][0] && binSearch(codepoint, table));
    }

    public static boolean isExtendedPictographic(int codepoint) {
        int[][] table = EmojiTable.EXTENDED_PICTOGRAPHIC_TABLE;
        return (codepoint >= table[0][0] && binSearch(codepoint, table));
    }

    public static boolean isEmojiBaseCharacter(int codepoint) {
        return isEmoji(codepoint);
    }

    public static boolean isEmojiExtendedPictographic(int codepoint) {
        return isExtendedPictographic(codepoint);
    }

    public static boolean isEmojiEmojiDefault(int codepoint) {
        return isEmojiPresentation(codepoint);
    }

    public static boolean isEmojiKeycapBase(int codepoint) {
        return (codepoint >= '0' && codepoint <= '9') || codepoint == '#' || codepoint == '*';
    }

    public static boolean isRegionalIndicator(int codepoint) {
        return (codepoint >= 0x1F1E6 && codepoint <= 0x1F1FF);
    }

    public static byte emojiSegmentationCategory(int codepoint) {
        /* Specific ones first. */
        if (('a' <= codepoint && codepoint <= 'z') ||
            ('A' <= codepoint && codepoint <= 'Z') ||
            codepoint == ' ') {
            return EmojiCategory.MAX;
        }

        if ('0' <= codepoint && codepoint <= '9') {
            return EmojiCategory.KEYCAP_BASE;
        }

        switch (codepoint) {
            case 0x20E3:
                return COMBINING_ENCLOSING_KEYCAP;
            case 0x20E0:
                return COMBINING_ENCLOSING_CIRCLE_BACKSLASH;
            case 0x200D:
                return ZWJ;
            case 0xFE0E:
                return VS15;
            case 0xFE0F:
                return VS16;
            case 0x1F3F4:
                return TAG_BASE;
            case 0xE007F:
                return TAG_TERM;
            default:
        }

        if ((0xE0030 <= codepoint && codepoint <= 0xE0039) ||
            (0xE0061 <= codepoint && codepoint <= 0xE007A)) {
            return TAG_SEQUENCE;
        } else if (isEmojiModifierBase(codepoint)) {
            return EMOJI_MODIFIER_BASE;
        } else if (isEmojiModifier(codepoint)) {
            return EMOJI_MODIFIER;
        } else if (isRegionalIndicator(codepoint)) {
            return REGIONAL_INDICATOR;
        } else if (isEmojiKeycapBase(codepoint)) {
            return KEYCAP_BASE;
        } else if (isEmojiPresentation(codepoint)) {
            return EMOJI_EMOJI_PRESENTATION;
        } else if (isEmoji(codepoint)) {
            return EMOJI_TEXT_PRESENTATION;
        } else {
            /* Ragel state machine will interpret unknown category as "any". */
            return MAX;
        }
    }

    public Unichar[] getUnicodeChars() {
        return unichars;
    }
}
