package io.github.jmecn.font.generator.enums;

import static org.lwjgl.util.freetype.FreeType.*;

public enum Hinting {
    /** Disable hinting. Generated glyphs will look blurry. */
    NONE(FT_LOAD_NO_HINTING),
    /** Light hinting with fuzzy edges, but close to the original shape */
    LIGHT(FT_LOAD_NO_AUTOHINT | FT_FT_LOAD_TARGET_LIGHT),
    /** Average hinting */
    NORMAL(FT_LOAD_NO_AUTOHINT | FT_FT_LOAD_TARGET_NORMAL),
    /** Strong hinting with crisp edges at the expense of shape fidelity */
    MONO(FT_LOAD_NO_AUTOHINT | FT_FT_LOAD_TARGET_MONO),
    /** Light hinting with fuzzy edges, but close to the original shape. Uses the FreeType auto-hinter. */
    AUTO_LIGHT(FT_LOAD_FORCE_AUTOHINT | FT_FT_LOAD_TARGET_LIGHT),
    /** Average hinting. Uses the FreeType auto-hinter. */
    AUTO_NORMAL(FT_LOAD_FORCE_AUTOHINT | FT_FT_LOAD_TARGET_NORMAL),
    /** Strong hinting with crisp edges at the expense of shape fidelity. Uses the FreeType auto-hinter. */
    AUTO_MONO(FT_LOAD_FORCE_AUTOHINT | FT_FT_LOAD_TARGET_MONO);

    private final int loadFlags;

    Hinting(int loadFlags) {
        this.loadFlags = loadFlags;
    }

    public int getLoadFlags() {
        return loadFlags;
    }
}
