package io.github.jmecn.font;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class TestAwtFont {
    @Test void testGetAllFonts() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = env.getAllFonts();
//        for (Font font : fonts) {
//            System.out.println(font);
//        }
        assertTrue(0 < fonts.length);
    }
}
