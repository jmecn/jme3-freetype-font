package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import imgui.*;
import io.github.jmecn.font.CommonChars;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.editor.app.CheckerBoardState;
import io.github.jmecn.font.editor.app.LightState;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.plugins.FtFontLoader;

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

    public Main(AppState... states) {
        super(states);
        parameter = new FtFontParameter();
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

        ImGui.text("你好，世界");

        // 将 parameter 中的参数全部使用 imgui 绘制出来
        /*
        private int size = 16;
        private RenderMode renderMode = RenderMode.NORMAL;
        private int spread = 2;
        private Hinting hinting = Hinting.NORMAL;
        private ColorRGBA color = ColorRGBA.White;
        private float gamma = 1.8f;
        private int renderCount = 2;
        private float borderWidth = 0;
        private ColorRGBA borderColor = ColorRGBA.Black;
        private boolean borderStraight = false;
        private float borderGamma = 1.8f;
        private int shadowOffsetX = 0;
        private int shadowOffsetY = 0;
        private ColorRGBA shadowColor = new ColorRGBA(0, 0, 0, 0.75f);
        private int spaceX;
        private int spaceY;
        private int padTop;
        private int padLeft;
        private int padBottom;
        private int padRight;
        private String characters = CommonChars.ASCII.getChars();
        private boolean kerning = true;
        private Packer packer = null;
        private boolean genMipMaps = false;
        private Texture.MinFilter minFilter = Texture.MinFilter.NearestNoMipMaps;
        private Texture.MagFilter magFilter = Texture.MagFilter.Bilinear;
        private MaterialDef matDef;
        private String matDefName = "Common/MatDefs/Misc/Unshaded.j3md";
        private String colorMapParamName = "ColorMap";// or DiffuseMap in Lighting.j3md
        private boolean useVertexColor = true;
        private String vertexColorParamName = "VertexColor";
        private boolean incremental = false;
        */


        ImGui.checkbox("动态生成", parameter.isIncremental());


        // End the ImGui frame
        ImGuiJme3.endFrame();
    }
}
