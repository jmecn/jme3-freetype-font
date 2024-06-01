package io.github.jmecn.text;

import static io.github.jmecn.text.EmojiCategory.*;
import static io.github.jmecn.text.EmojiPresentationScanner.*;

/**
 * Implementation of EmojiIterator is based on Pango's pango-emoji.c
 *
 *
 * @author yanmaoyuan
 */
public class EmojiIterator {

    int textStart;
    int textEnd;
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

        textStart = 0;
        start = 0;
        end = 0;

        textEnd = nChars - 1;

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
        return unichars[start].getStart();
    }

    public int getTextEnd() {
        return unichars[end - 1].getEnd();
    }

    public boolean next() {
        if (this.end >= this.nChars) {
            return false;
        }

        this.start = this.end;

        int cursor = this.cursor;

        EmojiIteratorResult sr = scanEmojiPresentation(this.types, cursor, this.nChars);
        cursor = sr.end;
        boolean isEmoji = sr.isEmoji;

        do {
            this.cursor = cursor;
            this.isEmoji = isEmoji;

            if (cursor == this.nChars) {// end
                break;
            }

            sr = scanEmojiPresentation(this.types, cursor, this.nChars);
            cursor = sr.end;
            isEmoji = sr.isEmoji;
        } while (this.isEmoji == isEmoji);

        this.end = this.cursor;

        return true;
    }

    public Unichar[] getUnicodeChars() {
        return unichars;
    }
}
