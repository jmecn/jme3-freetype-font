package io.github.jmecn.text;

import com.jme3.scene.Node;

/**
 * The {@code Text} class defines a node that displays a text.
 *
 * Paragraphs are separated by {@code '\n'} and the text is wrapped on
 * paragraph boundaries.
 *
 * @author yanmaoyuan
 */
public class Text extends Node {

    private TextLayout layout;

    public Text() {}

    public Text(String text) {
    }
}
