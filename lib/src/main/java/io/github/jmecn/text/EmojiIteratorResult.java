package io.github.jmecn.text;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class EmojiIteratorResult {
    boolean isEmoji;
    int cursor;
    int end;
    EmojiIteratorResult(boolean isEmoji, int cursor, int end) {
        this.isEmoji = isEmoji;
        this.cursor = cursor;
        this.end = end;
    }

    EmojiIteratorResult(boolean isEmoji, int end) {
        this.isEmoji = isEmoji;
        this.end = end;
    }
}
