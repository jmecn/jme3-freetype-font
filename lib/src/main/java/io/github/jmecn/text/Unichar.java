package io.github.jmecn.text;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Unichar {
    int codepoint;
    int start;
    int end;
    byte emojiType;

    public int getCodepoint() {
        return codepoint;
    }

    public void setCodepoint(int codepoint) {
        this.codepoint = codepoint;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public byte getEmojiType() {
        return emojiType;
    }

    public void setEmojiType(byte emojiType) {
        this.emojiType = emojiType;
    }

    @Override
    public String toString() {
        return String.format("Unichar{codepoint=U+%X, range=[%d, %d), emojiType=%d:%s}",
                codepoint, start, end, emojiType, EmojiCategory.getName(emojiType));
    }
}
