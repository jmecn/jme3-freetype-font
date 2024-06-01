package io.github.jmecn.text;

public class EmojiCategory {
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
}