package io.github.jmecn.text;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.jmecn.text.EmojiCategory.*;
import static io.github.jmecn.text.EmojiPresentationScanner.*;

/**
 * Implementation of EmojiIterator is based on Pango's pango-emoji.c
 *
 * @see <a href="https://gitlab.gnome.org/GNOME/pango/-/blob/6fab48ac3a2d613704301d692e278cdcef3c2bf4/pango/pango-emoji.c">pango/pango-emoji.c</a>
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

        int p = this.cursor;

        int scanResult = scanEmojiPresentation(this.types, p, this.nChars);
        // because java do not have a referenced boolean type, here I use a bit mask to indicate whether the scanResult is emoji.
        // this also avoid instancing a new object.
        p = scanResult & 0x7FFFFFFF;
        boolean emoji = (scanResult & 0x80000000) != 0;

        do {
            this.cursor = p;
            this.isEmoji = emoji;

            if (p == this.nChars) {// end
                break;
            }

            scanResult = scanEmojiPresentation(this.types, p, this.nChars);
            p = scanResult & 0x7FFFFFFF;
            emoji = (scanResult & 0x80000000) != 0;
        } while (this.isEmoji == emoji);

        this.end = this.cursor;

        return true;
    }

    public Unichar[] getUnicodeChars() {
        return unichars;
    }
}
