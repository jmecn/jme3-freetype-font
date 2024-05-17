package io.github.jmecn.font.generator.enums;

import static org.lwjgl.util.freetype.FreeType.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public enum RenderMode {
    MONO(FT_RENDER_MODE_MONO), LIGHT(FT_RENDER_MODE_LIGHT), NORMAL(FT_RENDER_MODE_NORMAL), SDF(FT_RENDER_MODE_SDF);
    final int value;
    RenderMode(int value) {
        this.value = value;
    }

    public int getMode() {
        return value;
    }
}
