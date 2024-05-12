package io.github.jmecn.font;

import org.junit.jupiter.api.Test;
import sun.awt.FontConfiguration;

import java.awt.*;

public class GetAllFonts {
    @Test void testGetAllFonts() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = env.getAllFonts();
        for (int i = 0; i < fonts.length; i++) {
            System.out.println(fonts[i]);
        }
    }

    @Test void testFontConfig() {
        FontConfiguration fontConfig;
    }
}
