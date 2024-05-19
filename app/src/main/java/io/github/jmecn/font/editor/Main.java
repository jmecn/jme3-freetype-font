package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import imgui.*;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.jmecn.font.CommonChars;
import io.github.jmecn.font.editor.app.LightState;
import io.github.jmecn.font.freetype.FtLibrary;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.Hinting;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.plugins.FtFontLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

        Main app = new Main(new StatsAppState(), new DetailedProfilerState(), new FlyCamAppState());
        app.setSettings(settings);
        app.start();
    }

    static Logger logger = LoggerFactory.getLogger(Main.class);
    static final String TEXT = "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?\n" +
            "jMonkeyEngine is a modern developer friendly game engine written primarily in Java.\n";

    private final Node scene;

    private Packer packer;
    private FtFontGenerator generator;
    private final FtFontParameter parameter;

    ImString font = new ImString();

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
    ImInt strategy = new ImInt();// 0 - SkylineStrategy, 1 -
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
        scene = new Node("freetype-font");

        parameter = new FtFontParameter();

        font.set("font/FreeSerif.ttf");

        packerSize.set(256);
        packPadding.set(1);
        strategy.set(1);

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

        flyCam.setMoveSpeed(10f);
        flyCam.setDragToRotate(true);
        rootNode.attachChild(scene);

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
        //stateManager.attach(new CheckerBoardState());

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
        ImGui.combo("strategy", strategy, strategyOptions);
        ImGui.end();

        ImGui.begin("FtFontParameters");


        ImGui.inputInt("size", size);
        ImGui.combo("renderMode", renderMode, renderModes);

        ImGui.colorEdit4("color", color);
        ImGui.sliderFloat("gamma", gamma.getData(), 1.0f, 2.2f);
        ImGui.inputInt("renderCount", renderCount);
        ImGui.sliderInt("spread", spread.getData(), FtLibrary.MIN_SPREAD, FtLibrary.MAX_SPREAD);
        ImGui.combo("hinting", hinting, hintings);
        ImGui.checkbox("kerning", kerning);
        ImGui.checkbox("incremental", incremental);

        // 将 parameter 中的参数全部使用 imgui 绘制出来

        ImGui.inputFloat("borerWidth", borderWidth);
        ImGui.colorEdit4("borderColor", borderColor);
        ImGui.checkbox("borderStraight", borderStraight);
        ImGui.sliderFloat("borderGamma", borderGamma.getData(), 1.0f, 2.2f);

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
            getParameter();
        }
        ImGui.end();

        // End the ImGui frame
        ImGuiJme3.endFrame();
    }

    private void getParameter() {
        PackStrategy packStrategy;
        if (strategy.get() == 0) {
            packStrategy = new GuillotineStrategy();
        } else {
            packStrategy = new SkylineStrategy();
        }

        if (packer != null) {
            packer.close();
            packer = null;
        }

        packer = new Packer(Image.Format.RGBA8, packerSize.get(), packerSize.get(), packPadding.get(),  false, packStrategy);

        parameter.setPacker(packer);
        parameter.setSize(size.get());
        parameter.setRenderMode(RenderMode.valueOf(renderModes[renderMode.get()]));
        parameter.setColor(new ColorRGBA(color[0], color[1], color[2], color[3]));
        parameter.setGamma(gamma.get());
        parameter.setRenderCount(renderCount.get());
        parameter.setSpread(spread.get());
        parameter.setHinting(Hinting.valueOf(hintings[hinting.get()]));
        parameter.setKerning(kerning.get());
        parameter.setIncremental(incremental.get());

        parameter.setBorderWidth(borderWidth.get());
        parameter.setBorderColor(new ColorRGBA(borderColor[0], borderColor[1], borderColor[2], borderColor[3]));
        parameter.setBorderStraight(borderStraight.get());
        parameter.setBorderGamma(borderGamma.get());

        parameter.setShadowOffsetX(shadowOffsetX.get());
        parameter.setShadowOffsetY(shadowOffsetY.get());
        parameter.setShadowColor(new ColorRGBA(shadowColor[0], shadowColor[1], shadowColor[2], shadowColor[3]));

        parameter.setSpaceX(spaceX.get());
        parameter.setSpaceY(spaceY.get());

        parameter.setPadLeft(padLeft.get());
        parameter.setPadRight(padRight.get());
        parameter.setPadTop(padTop.get());
        parameter.setPadBottom(padBottom.get());

        parameter.setGenMipMaps(genMipMaps.get());
        parameter.setMinFilter(Texture.MinFilter.valueOf(minFilterOptions[minFilter.get()]));
        parameter.setMagFilter(Texture.MagFilter.valueOf(magFilterOptions[magFilter.get()]));

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>(parameter.getMatDefName()));
        parameter.setMatDef(matDef);

        if (generator != null) {
            // TODO clean up
            generator.close();
        }

        generator = new FtFontGenerator(new File(font.get()), 0);

        BitmapFont font = generator.generateFont(parameter);

        scene.detachAllChildren();
        buildFtBitmapText(font);

    }

    private Geometry buildFontPage(BitmapFont font, int i) {
        Geometry page = new Geometry("page#" + i, new Quad(6, 6));
        Material mat = font.getPage(i);
        Material unshaded = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        unshaded.setTexture("ColorMap", mat.getTextureParam("ColorMap").getTextureValue());
        page.setMaterial(unshaded);
        return page;
    }

    private void buildFtBitmapText(BitmapFont fnt) {
        Quad q = new Quad(6, 5);
        Geometry g = new Geometry("quad", q);
        g.setLocalTranslation(0, -5, -0.0001f);
        g.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        scene.attachChild(g);
        BitmapText txt = new BitmapText(fnt);
        txt.setBox(new Rectangle(0, 0, 6, 5));
        txt.setQueueBucket(RenderQueue.Bucket.Transparent);
        txt.setSize( 0.5f );
        txt.setText(TEXT);
        scene.attachChild(txt);

        // show font images
        int pageSize = fnt.getPageSize();
        for (int i = 0; i < pageSize; i++) {
            Geometry g2 = buildFontPage(fnt, i);
            g2.setLocalTranslation(0, i * 6f, 0);
            scene.attachChild(g2);
        }

        logger.info("scene:{}", scene.getChildren());
    }
}
