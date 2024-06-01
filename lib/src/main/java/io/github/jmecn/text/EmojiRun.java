package io.github.jmecn.text;

import java.util.Objects;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class EmojiRun {

    private final boolean isEmoji;
    private final int unicodeStart;
    private final int unicodeEnd;
    private final int textStart;
    private final int textEnd;

    EmojiRun(boolean isEmoji, int unicodeStart, int unicodeEnd, int textStart, int textEnd) {
        this.isEmoji = isEmoji;
        this.unicodeStart = unicodeStart;
        this.unicodeEnd = unicodeEnd;
        this.textStart = textStart;
        this.textEnd = textEnd;
    }

    public boolean isEmoji() {
        return isEmoji;
    }

    public int getUnicodeStart() {
        return unicodeStart;
    }

    public int getUnicodeEnd() {
        return unicodeEnd;
    }

    public int getTextStart() {
        return textStart;
    }

    public int getTextEnd() {
        return textEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmojiRun)) {
            return false;
        }
        EmojiRun that = (EmojiRun) o;
        return isEmoji == that.isEmoji && unicodeStart == that.unicodeStart && unicodeEnd == that.unicodeEnd && textStart == that.textStart && textEnd == that.textEnd;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEmoji, unicodeStart, unicodeEnd, textStart, textEnd);
    }
}
