package io.github.jmecn.font.harfbuzz;

import static org.lwjgl.util.harfbuzz.HarfBuzz.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class HbFace implements AutoCloseable {
    long address;

    public HbFace(long address) {
        this.address = address;
    }

    @Override
    public void close() {
        hb_face_destroy(address);
    }

    public HbFont createFont() {
        long font = hb_font_create(address);
        return new HbFont(font);
    }
}
