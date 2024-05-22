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

    static final String[] LANGUAGES = new String[]{
            "en",
            "zh_CN"
    };

    ////////////// keys /////////////////
    public static final String TITLE = "title";
    public static final String MAIN_TITLE = "main.title";
    public static final String MENU_FILE = "menu.file";
    public static final String MENU_FILE_LOAD = "menu.file.load";
    public static final String MENU_FILE_OPEN = "menu.file.open";
    public static final String MENU_FILE_SAVE = "menu.file.save";
    public static final String MENU_FILE_SAVE_AS = "menu.file.saveAs";
    public static final String MENU_VIEW = "menu.view";
    public static final String MENU_VIEW_TEXT = "menu.view.text";
    public static final String MENU_VIEW_PAGES = "menu.view.pages";
    public static final String MENU_CONFIG = "menu.config";
    public static final String MENU_LANGUAGE = "menu.language";
    public static final String IMAGES_TITLE = "images.title";
    public static final String IMAGES_EMPTY = "images.empty";
    public static final String TEXT_TITLE = "text.title";
    public static final String FONT_TITLE = "font.title";
    public static final String FONT_FILE = "font.file";
    public static final String FONT_SIZE = "font.size";
    public static final String FONT_KERNING = "font.kerning";
    public static final String FONT_INCREMENTAL = "font.incremental";
    public static final String PACK_TITLE = "pack.title";
    public static final String PACK_WIDTH = "pack.width";
    public static final String PACK_HEIGHT = "pack.height";
    public static final String PACK_PADDING = "pack.padding";
    public static final String PACK_STRATEGY = "pack.strategy";
    public static final String RENDER_TITLE = "render.title";
    public static final String RENDER_HINTING = "render.hinting";
    public static final String RENDER_MODE = "render.mode";
    public static final String RENDER_SPREAD = "render.spread";
    public static final String RENDER_COLOR = "render.color";
    public static final String RENDER_GAMMA = "render.gamma";
    public static final String RENDER_COUNT = "render.count";
    public static final String BORDER_TITLE = "border.title";
    public static final String BORDER_WIDTH = "border.width";
    public static final String BORDER_COLOR = "border.color";
    public static final String BORDER_GAMMA = "border.gamma";
    public static final String BORDER_STRAIGHT = "border.straight";
    public static final String SHADOW_TITLE = "shadow.title";
    public static final String SHADOW_OFFSET_X = "shadow.offsetX";
    public static final String SHADOW_OFFSET_Y = "shadow.offsetY";
    public static final String SHADOW_COLOR = "shadow.color";
    public static final String SPACE_TITLE = "space.title";
    public static final String SPACE_X = "space.x";
    public static final String SPACE_Y = "space.y";
    public static final String PADDING_TITLE = "padding.title";
    public static final String PADDING_LEFT = "padding.left";
    public static final String PADDING_RIGHT = "padding.right";
    public static final String PADDING_TOP = "padding.top";
    public static final String PADDING_BOTTOM = "padding.bottom";
    public static final String MATERIAL_TITLE = "material.title";
    public static final String MATERIAL_DEFINE = "material.define";
    public static final String MATERIAL_COLOR_MAP = "material.colorMap";
    public static final String MATERIAL_VERTEX_COLOR = "material.vertexColor";
    public static final String MATERIAL_USE_VERTEX_COLOR = "material.useVertexColor";
    public static final String TEXTURE_MIN_FILTER = "texture.minFilter";
    public static final String TEXTURE_MAG_FILTER = "texture.magFilter";

    public static final String DIALOG_FONT_OPEN = "dialog.font.open";
    public static final String DIALOG_FONT_DESC = "dialog.font.desc";
    public static final String DIALOG_PRESET_OPEN = "dialog.preset.open";
    public static final String DIALOG_PRESET_SAVE = "dialog.preset.save";
    public static final String DIALOG_PRESET_DESC = "dialog.preset.desc";
}