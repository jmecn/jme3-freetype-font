package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import imgui.*;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.jmecn.font.CommonChars;
import io.github.jmecn.font.editor.app.CheckerBoardState;
import io.github.jmecn.font.editor.app.LightState;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.plugins.FtFontLoader;

import java.util.Arrays;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Main extends SimpleApplication {
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setTitle("Freetype font editor");
        settings.setSamples(4);

        Main app = new Main(new StatsAppState(), new DetailedProfilerState());
        app.setSettings(settings);
        app.start();
    }

    private final FtFontParameter parameter;

    // here is all the FtFontParameters, for imgui
    ImInt size = new ImInt();
    ImInt renderMode = new ImInt();
    String[] renderModes;

    ImInt spread = new ImInt();
    ImInt hinting = new ImInt();
    String[] hintings;
    ImBoolean kerning = new ImBoolean();
    ImBoolean incremental = new ImBoolean();
    ImString characters = new ImString();

    // packer
    ImInt packerSize = new ImInt();
    ImInt packPadding = new ImInt();
    ImInt packStrategy = new ImInt();// 0 - SkylineStrategy, 1 -
    String[] strategyOptions;

    // color
    float[] color = new float[4];// rgba
    ImFloat gamma = new ImFloat();
    ImInt renderCount = new ImInt();

    // border
    ImFloat borderWidth = new ImFloat();
    float[] borderColor = new float[4];
    ImBoolean borderStraight = new ImBoolean();
    ImFloat borderGamma = new ImFloat();
    // shadow
    ImInt shadowOffsetX = new ImInt();
    ImInt shadowOffsetY = new ImInt();
    float[] shadowColor = new float[4];
    // spacing
    ImInt spaceX = new ImInt();
    ImInt spaceY = new ImInt();

    // padding
    ImInt padTop = new ImInt();
    ImInt padLeft = new ImInt();
    ImInt padBottom = new ImInt();
    ImInt padRight = new ImInt();

    // texture
    ImBoolean genMipMaps = new ImBoolean();
    ImInt minFilter = new ImInt();
    String[] minFilterOptions;
    ImInt magFilter = new ImInt();
    String[] magFilterOptions;

    // materials
    ImString matDefName = new ImString();
    ImString colorMapParamName = new ImString();
    ImString vertexColorParamName = new ImString();
    ImBoolean useVertexColor = new ImBoolean();

    public Main(AppState... states) {
        super(states);
        parameter = new FtFontParameter();

        packerSize.set(1024);
        packPadding.set(1);
        packStrategy.set(0);

        renderModes = Arrays.stream(RenderMode.values()).map(RenderMode::name).toArray(String[]::new);
        hintings = Arrays.stream(Hinting.values()).map(Hinting::name).toArray(String[]::new);
        strategyOptions = new String[] {GuillotineStrategy.class.getSimpleName(), SkylineStrategy.class.getSimpleName()};
        minFilterOptions = Arrays.stream(Texture.MinFilter.values()).map(Texture.MinFilter::name).toArray(String[]::new);
        magFilterOptions = Arrays.stream(Texture.MagFilter.values()).map(Texture.MagFilter::name).toArray(String[]::new);
    }

    /**
     * set parameter to imgui
     * @param params
     */
    private void setParameter(FtFontParameter params) {
        size.set(params.getSize());
        renderMode.set(params.getRenderMode().ordinal());
        spread.set(params.getSpread());
        hinting.set(params.getHinting().ordinal());
        kerning.set(params.isKerning());
        incremental.set(params.isIncremental());

        characters.set(params.getCharacters());

        color = params.getColor().toArray(color);
        gamma.set(params.getGamma());
        renderCount.set(params.getRenderCount());

        borderWidth.set(params.getBorderWidth());
        borderColor = params.getBorderColor().toArray(borderColor);
        borderStraight.set(params.isBorderStraight());
        borderGamma.set(params.getBorderGamma());

        shadowOffsetX.set(params.getShadowOffsetX());
        shadowOffsetY.set(params.getShadowOffsetY());
        shadowColor = params.getShadowColor().toArray(shadowColor);

        spaceX.set(params.getSpaceX());
        spaceY.set(params.getSpaceY());

        padTop.set(params.getPadTop());
        padLeft.set(params.getPadLeft());
        padBottom.set(params.getPadBottom());
        padRight.set(params.getPadRight());

        genMipMaps.set(params.isGenMipMaps());
        minFilter.set(params.getMinFilter().ordinal());
        magFilter.set(params.getMagFilter().ordinal());

        matDefName.set(params.getMatDefName());
        colorMapParamName.set(params.getColorMapParamName());
        vertexColorParamName.set(params.getVertexColorParamName());
        useVertexColor.set(params.isUseVertexColor());
    }


    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(FtFontLoader.class, "otf", "ttf");

        // hide stats and profiler by default
        StatsAppState statsAppState = stateManager.getState(StatsAppState.class);
        statsAppState.initialize(stateManager, this);
        statsAppState.setDisplayStatView(false);
        statsAppState.setDisplayFps(false);

        DetailedProfilerState profilerState = stateManager.getState(DetailedProfilerState.class);
        profilerState.initialize(stateManager, this);
        profilerState.setEnabled(false);

        // init sky
        Spatial sky = SkyFactory.createSky(assetManager, "sky/env1.hdr", SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        // init imgui
        initImGui();
        setParameter(parameter);

        ///// init app state /////
        stateManager.attach(new LightState());
        stateManager.attach(new CheckerBoardState());

        // init camera
        cam.setLocation(new Vector3f(0f, 3f, 10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFov(60);
    }

    private void initImGui() {
        ImGuiJme3.initialize(this);
        ImGui.getIO().setIniFilename(null);
        // Load custom font
        ImGuiIO imGuiIO = ImGui.getIO();

        imGuiIO.getFonts().addFontDefault(); // Add default font for latin glyphs

        // You can use the ImFontGlyphRangesBuilder helper to create glyph ranges based on text input.
        // For example: for a game where your script is known, if you can feed your entire script to it (using addText) and only build the characters the game needs.
        // Here we are using it just to combine all required glyphs in one place
        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();          // Glyphs ranges provide
        rangesBuilder.addRanges(imGuiIO.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(imGuiIO.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(imGuiIO.getFonts().getGlyphRangesJapanese());
        rangesBuilder.addText(CommonChars.SIMPLIFIED_CHINESE.getChars());
        //rangesBuilder.addRanges(io.getFonts().getGlyphRangesChineseSimplifiedCommon()); // Seems broken
        //rangesBuilder.addRanges(io.getFonts().getGlyphRangesChineseFull());             // Seems broken

        // Font config for custom fonts
        ImFontConfig imFontConfig = new ImFontConfig();
        imFontConfig.setMergeMode(true);      // Merge Default, Cyrillic, Japanese ranges and manual specific chars

        final short[] glyphRanges = rangesBuilder.buildRanges();
        //
        ImFont imFont = imGuiIO.getFonts().addFontFromFileTTF("font/Noto_Serif_SC/NotoSerifSC-Regular.otf",
                16f, imFontConfig, glyphRanges);
        imGuiIO.getFonts().build();           // Build custom font
        imGuiIO.setFontDefault(imFont);       // Set custom font to default

        ImGuiJme3.refreshFontTexture();        // Don't forget to refresh the font texture!

        imFontConfig.destroy();               // Destroy the font config
    }

    @Override
    public void destroy() {
        ImGuiJme3.dispose();
        super.destroy();
    }

    public void simpleRender(RenderManager rm) {
        // Start the ImGui frame
        ImGuiJme3.startFrame();

        // menubar

        ImGui.begin("Packer");
        ImGui.inputInt("size", packerSize);
        ImGui.inputInt("padding", packPadding);
        ImGui.combo("strategy", packStrategy, strategyOptions);
        ImGui.end();

        ImGui.begin("FtFontParameters");


        ImGui.inputInt("size", size);
        ImGui.combo("renderMode", renderMode, renderModes);

        ImGui.colorEdit4("color", color);
        ImGui.inputFloat("gamma", gamma);
        ImGui.inputInt("renderCount", renderCount);
        ImGui.inputInt("spread", spread);
        ImGui.combo("hinting", hinting, hintings);
        ImGui.checkbox("kerning", kerning);
        ImGui.checkbox("incremental", incremental);

        // 将 parameter 中的参数全部使用 imgui 绘制出来

        ImGui.inputFloat("borerWidth", borderWidth);
        ImGui.colorEdit4("borderColor", borderColor);
        ImGui.checkbox("borderStraight", borderStraight);
        ImGui.inputFloat("borderGamma", borderGamma);

        ImGui.inputInt("shadowOffsetX", shadowOffsetX);
        ImGui.inputInt("shadowOffsetY", shadowOffsetY);
        ImGui.colorEdit4("shadowColor", shadowColor);

        ImGui.inputInt("spaceX", spaceX);
        ImGui.inputInt("spaceY", spaceY);

        ImGui.inputInt("padLeft", padLeft);
        ImGui.inputInt("padRight", padRight);
        ImGui.inputInt("padTop", padTop);
        ImGui.inputInt("padBottom", padBottom);

        ImGui.checkbox("genMipMaps", genMipMaps);
        ImGui.combo("minFilter", minFilter, minFilterOptions);
        ImGui.combo("magFilter", magFilter, magFilterOptions);

        if (ImGui.button("Generate")) {
            parameter.setIncremental(incremental.get());
        }
        ImGui.end();

        // End the ImGui frame
        ImGuiJme3.endFrame();
    }
}
