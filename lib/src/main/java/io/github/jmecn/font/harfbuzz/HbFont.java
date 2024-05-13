package io.github.jmecn.font.harfbuzz;

import static org.lwjgl.util.harfbuzz.HarfBuzz.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class HbFont implements AutoCloseable {
    long address;

    public HbFont(long address) {
        this.address = address;
    }

    @Override
    public void close() throws Exception {
        hb_font_destroy(address);
    }
}
