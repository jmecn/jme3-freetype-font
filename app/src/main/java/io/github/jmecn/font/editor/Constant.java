package io.github.jmecn.font.editor;

import com.jme3.material.Materials;
import com.jme3.texture.Texture;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;

import java.util.Arrays;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class Constant {
    private Constant() {}

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 100;

    static final String TEXT = "The quick brown fox jumps over the lazy dog.\n" +
            "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG.";

    static final String[] PACKER_SIZE_OPTIONS = new String[]{"128", "256", "512", "1024", "2048", "4096"};
    static final String[] STRATEGY_OPTIONS = new String[] {
            GuillotineStrategy.class.getSimpleName(),
            SkylineStrategy.class.getSimpleName()
    };
    static final String[] MAT_DEF_OPTIONS = new String[] {
            Materials.UNSHADED,
            "Shaders/Font/SdFont.j3md"
    };
    static final String[] RENDER_MODE_OPTIONS = Arrays.stream(RenderMode.values()).map(RenderMode::name).toArray(String[]::new);
    static final String[] HINTING_OPTIONS = Arrays.stream(Hinting.values()).map(Hinting::name).toArray(String[]::new);
    static final String[] MIN_FILTER_OPTIONS = Arrays.stream(Texture.MinFilter.values()).map(Texture.MinFilter::name).toArray(String[]::new);
    static final String[] MAG_FILTER_OPTIONS = Arrays.stream(Texture.MagFilter.values()).map(Texture.MagFilter::name).toArray(String[]::new);

}