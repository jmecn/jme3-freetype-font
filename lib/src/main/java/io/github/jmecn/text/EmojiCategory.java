package io.github.jmecn.text;

public final class EmojiCategory {
    private EmojiCategory() {}

    public static final byte EMOJI = 0;
    public static final byte EMOJI_TEXT_PRESENTATION = 1;
    public static final byte EMOJI_EMOJI_PRESENTATION = 2;
    public static final byte EMOJI_MODIFIER_BASE = 3;
    public static final byte EMOJI_MODIFIER = 4;
    public static final byte EMOJI_VS_BASE = 5;
    public static final byte REGIONAL_INDICATOR = 6;
    public static final byte KEYCAP_BASE = 7;
    public static final byte COMBINING_ENCLOSING_KEYCAP = 8;
    public static final byte COMBINING_ENCLOSING_CIRCLE_BACKSLASH = 9;
    public static final byte ZWJ = 10;
    public static final byte VS15 = 11;
    public static final byte VS16 = 12;
    public static final byte TAG_BASE = 13;
    public static final byte TAG_SEQUENCE = 14;
    public static final byte TAG_TERM = 15;
    public static final byte MAX = 16;

    static final String[] NAMES = new String[] {
            "EMOJI",
            "EMOJI_TEXT_PRESENTATION",
            "EMOJI_EMOJI_PRESENTATION",
            "EMOJI_MODIFIER_BASE",
            "EMOJI_MODIFIER",
            "EMOJI_VS_BASE",
            "REGIONAL_INDICATOR",
            "KEYCAP_BASE",
            "COMBINING_ENCLOSING_KEYCAP",
            "COMBINING_ENCLOSING_CIRCLE_BACKSLASH",
            "ZWJ",
            "VS15",
            "VS16",
            "TAG_BASE",
            "TAG_SEQUENCE",
            "TAG_TERM",
            "MAX"
    };
    public static String getName(byte category) {
        return NAMES[category];
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
}