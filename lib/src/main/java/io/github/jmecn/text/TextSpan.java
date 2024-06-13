package io.github.jmecn.text;

import io.github.jmecn.font.Font;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface TextSpan {
    /**
     * The text for the span, can be empty but not null.
     */
    String getText();

    /**
     * The font for the span, if null the span is handled as embedded object.
     */
    Font getFont();

    /**
     * The bounds for embedded object, only used the font returns null.
     * The text for a embedded object should be a single char ("\uFFFC" is
     * recommended).
     */
    RectBounds getBounds();
}
