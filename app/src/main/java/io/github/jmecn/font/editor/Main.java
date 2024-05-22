package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.MaterialDef;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import imgui.*;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiWindowFlags;
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
import io.github.jmecn.font.packer.Page;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.plugins.FtFontLoader;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.system.MemoryStack.stackPush;

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

    static Logger logger = LoggerFactory.getLogger(Main.class);
    static final String TEXT = "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?\n" +
            "jMonkeyEngine is a modern developer friendly game engine written primarily in Java.\n";

    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    // this is an indicator for loading font. max 1
    private final AtomicInteger loadCount = new AtomicInteger(0);

    private final Node scene;

    /////////////// bitmapfont bitmaptext ////////////////
    private Packer packer;
    private FtFontGenerator generator;
    private final FtFontParameter parameter;
    private BitmapFont bmfont;
    private BitmapText bmtext;

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
    ImString content = new ImString();

    // packer
    ImInt packerWidth = new ImInt();
    ImInt packerHeight = new ImInt();
    String[] packerSizes = new String[]{"128", "256", "512", "1024", "2048", "4096"};
    ImInt packPadding = new ImInt();
    ImInt strategy = new ImInt();// 0 - SkylineStrategy, 1 -
    String[] strategyOptions;

    // color
    float[] color = new float[4];// rgba
    ImFloat gamma = new ImFloat();
    ImInt renderCount = new ImInt();

    // border
    ImInt borderWidth = new ImInt();
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
    ImInt minFilter = new ImInt();
    String[] minFilterOptions;
    ImInt magFilter = new ImInt();
    String[] magFilterOptions;

    // materials
    ImInt matDefId = new ImInt();
    String[] matDefs;
    ImString colorMapParamName = new ImString();
    ImString vertexColorParamName = new ImString();
    ImBoolean useVertexColor = new ImBoolean();


    public Main(AppState... states) {
        super(states);
        scene = new Node("freetype-font");

        parameter = new FtFontParameter();

        strategyOptions = new String[] {
                GuillotineStrategy.class.getSimpleName(),
                SkylineStrategy.class.getSimpleName()
        };
        matDefs = new String[] {
                Materials.UNSHADED,
                "Shaders/Font/SdFont.j3md"
        };

        renderModes = Arrays.stream(RenderMode.values()).map(RenderMode::name).toArray(String[]::new);
        hintings = Arrays.stream(Hinting.values()).map(Hinting::name).toArray(String[]::new);
        minFilterOptions = Arrays.stream(Texture.MinFilter.values()).map(Texture.MinFilter::name).toArray(String[]::new);
        magFilterOptions = Arrays.stream(Texture.MagFilter.values()).map(Texture.MagFilter::name).toArray(String[]::new);
    }

    /**
     * set parameter to imgui
     */
    private void setParameter() {
        size.set(parameter.getSize());

        // packer
        if (packer != null) {
            packerWidth.set(packer.getPageWidth());
            packerHeight.set(packer.getPageHeight());
            packPadding.set(packer.getPadding());
            int index = Arrays.asList(strategyOptions).indexOf(packer.getPackStrategy().getClass().getSimpleName());
            if (index > -1) {
                strategy.set(index);
            } else {
                strategy.set(0);
            }
        } else {
            packerWidth.set(256);
            packerHeight.set(256);
            packPadding.set(1);
            strategy.set(0);
        }

        renderMode.set(parameter.getRenderMode().ordinal());
        spread.set(parameter.getSpread());
        hinting.set(parameter.getHinting().ordinal());
        kerning.set(parameter.isKerning());
        incremental.set(parameter.isIncremental());

        content.set(TEXT);

        color = parameter.getColor().toArray(color);
        gamma.set(parameter.getGamma());
        renderCount.set(parameter.getRenderCount());

        borderWidth.set(parameter.getBorderWidth());
        borderColor = parameter.getBorderColor().toArray(borderColor);
        borderStraight.set(parameter.isBorderStraight());
        borderGamma.set(parameter.getBorderGamma());

        shadowOffsetX.set(parameter.getShadowOffsetX());
        shadowOffsetY.set(parameter.getShadowOffsetY());
        shadowColor = parameter.getShadowColor().toArray(shadowColor);

        spaceX.set(parameter.getSpaceX());
        spaceY.set(parameter.getSpaceY());

        padTop.set(parameter.getPadTop());
        padLeft.set(parameter.getPadLeft());
        padBottom.set(parameter.getPadBottom());
        padRight.set(parameter.getPadRight());

        minFilter.set(parameter.getMinFilter().ordinal());
        magFilter.set(parameter.getMagFilter().ordinal());

        int index = Arrays.asList(matDefs).indexOf(parameter.getMatDefName());
        if (index > -1) {
            matDefId.set(index);
        } else {
            matDefId.set(0);
        }
        colorMapParamName.set(parameter.getColorMapParamName());
        vertexColorParamName.set(parameter.getVertexColorParamName());
        useVertexColor.set(parameter.isUseVertexColor());
    }


    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(FtFontLoader.class, "otf", "ttf");

        guiNode.attachChild(scene);
        scene.setLocalTranslation(380, 0, 0);

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
        setParameter();

        ///// init app state /////
        stateManager.attach(new LightState());

        // init camera
        cam.setLocation(new Vector3f(0f, 3f, 20f));
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
        rangesBuilder.addText(CommonChars.SIMPLIFIED_CHINESE.getChars());

        // Font config for custom fonts
        ImFontConfig imFontConfig = new ImFontConfig();
        imFontConfig.setMergeMode(true);      // Merge Default, Cyrillic, Japanese ranges and manual specific chars

        final short[] glyphRanges = rangesBuilder.buildRanges();
        //
        ImFont imFont = imGuiIO.getFonts().addFontFromMemoryTTF(getResourcesAsBytes("font/unifont-15.1.05.otf"), 12f, imFontConfig, glyphRanges);
        imGuiIO.getFonts().build();           // Build custom font
        imGuiIO.setFontDefault(imFont);       // Set custom font to default

        ImGuiJme3.refreshFontTexture();        // Don't forget to refresh the font texture!

        imFontConfig.destroy();               // Destroy the font config
    }

    private static byte[] getResourcesAsBytes(String resource) {
        try {
            return Files.readAllBytes(Paths.get(Objects.requireNonNull(Main.class.getClassLoader().getResource(resource)).toURI()));
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to read resource: {}", resource, e);
        }
        return new byte[0];
    }

    @Override
    public void destroy() {
        ImGuiJme3.dispose();
        if (packer != null) {
            packer.close();
        }
        if (generator != null) {
            generator.close();
        }
        super.destroy();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (bmtext != null) {
            bmtext.setText(content.get());
        }
    }
    @Override
    public void simpleRender(RenderManager rm) {
        // Start the ImGui frame
        ImGuiJme3.startFrame();

        ImGui.setNextWindowSize(360, 0);
        ImGui.setNextWindowPos(0, 0);
        showParameterWindow();

        // End the ImGui frame
        ImGuiJme3.endFrame();
    }

    private void showParameterWindow() {
        ImGui.begin("FtFontParameters", ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoTitleBar);

        boolean parameterChanged = false;

        showMenuBar();

        ImGui.pushItemWidth(300);
        ImGui.inputText("##font", font, ImGuiInputTextFlags.ReadOnly);
        if (ImGui.button("Load Font")) {
            loadFont();
        }
        ImGui.sameLine();
        if (ImGui.button("Preview")) {
            getParameter();
        }

        if (ImGui.collapsingHeader("Image packer")) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.combo("width", packerWidth, packerSizes);
            ImGui.sameLine();
            parameterChanged |= ImGui.combo("height", packerHeight, packerSizes);
            parameterChanged |= ImGui.dragInt("padding", packPadding.getData(), 1f, 0f, 100f);
            ImGui.pushItemWidth(200);
            parameterChanged |= ImGui.combo("strategy", strategy, strategyOptions);
        }

        if (packer != null && ImGui.collapsingHeader("Font Pages")
                && (ImGui.beginTabBar("Images", ImGuiTabBarFlags.None))) {
            //// blow we display the font images
            int i = 0;
            for (Page page : packer.getPages()) {
                if (ImGui.beginTabItem("page#" + i++)) {
                    ImGui.image(page.getImage().getId(), page.getImage().getWidth(), page.getImage().getHeight(), 0f, 1f, 1f, 0f);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        }

        ImGui.separator();
        ImGui.text("General");
        ImGui.pushItemWidth(80);
        parameterChanged |= ImGui.combo("render mode", renderMode, renderModes);
        if (renderMode.get() == RenderMode.SDF.ordinal()) {
            ImGui.sameLine();
            parameterChanged |= ImGui.sliderInt("spread", spread.getData(), FtLibrary.MIN_SPREAD, FtLibrary.MAX_SPREAD);
        }
        parameterChanged |= ImGui.combo("hinting", hinting, hintings);
        ImGui.pushItemWidth(100);
        parameterChanged |= ImGui.inputInt("font size", size);

        ImGui.pushItemWidth(200);
        parameterChanged |= ImGui.colorEdit4("color", color);
        ImGui.pushItemWidth(100);
        parameterChanged |= ImGui.inputFloat("gamma", gamma, 0.1f);
        parameterChanged |= ImGui.sliderInt("render count", renderCount.getData(), 1, 10);
        parameterChanged |= ImGui.checkbox("kerning", kerning);
        parameterChanged |= ImGui.checkbox("incremental", incremental);

        if (ImGui.collapsingHeader("Material Def")) {
            ImGui.pushItemWidth(280);
            parameterChanged |= ImGui.combo("MatDef", matDefId, matDefs);
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.inputText("ColorMapParamName", colorMapParamName);
            parameterChanged |= ImGui.inputText("VertexColorParamName", vertexColorParamName);
            parameterChanged |= ImGui.checkbox("UseVertexColor", useVertexColor);
        }

        if (ImGui.collapsingHeader("Text")) {
            ImGui.inputTextMultiline("##text", content, 300, 100, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.CallbackEdit);
        }

        if (ImGui.collapsingHeader("Border")) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt("borerWidth", borderWidth.getData(), 0, 10);
            ImGui.sameLine();
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.inputFloat("borderGamma", borderGamma, 0.1f);
            ImGui.pushItemWidth(200);
            parameterChanged |= ImGui.colorEdit4("borderColor", borderColor);
            parameterChanged |= ImGui.checkbox("borderStraight", borderStraight);
        }

        if (ImGui.collapsingHeader("ShadowOffset")) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt("offsetX", shadowOffsetX.getData(), -10, 10);
            ImGui.sameLine();
            parameterChanged |= ImGui.sliderInt("offsetY", shadowOffsetY.getData(), -10, 10);
            ImGui.pushItemWidth(200);
            parameterChanged |= ImGui.colorEdit4("shadowColor", shadowColor);
        }

        if (ImGui.collapsingHeader("Spacing")) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt("spaceX", spaceX.getData(), 0, 100);
            ImGui.sameLine();
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt("spaceY", spaceY.getData(), 0, 100);
        }

        if (ImGui.collapsingHeader("Padding")) {
            ImGui.indent(50);
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt("top", padTop.getData(), 0, 10);
            ImGui.indent(-50);
            parameterChanged |= ImGui.sliderInt("left", padLeft.getData(), 0, 10);
            ImGui.sameLine();
            parameterChanged |= ImGui.sliderInt("right", padRight.getData(), 0, 10);
            ImGui.indent(50);
            parameterChanged |= ImGui.sliderInt("bottom", padBottom.getData(), 0, 10);
            ImGui.indent(-50);
        }

        if (ImGui.collapsingHeader("Texture Filter")) {
            ImGui.pushItemWidth(200);
            parameterChanged |= ImGui.combo("minFilter", minFilter, minFilterOptions);
            parameterChanged |= ImGui.combo("magFilter", magFilter, magFilterOptions);
        }

        if (parameterChanged) {
            logger.info("changed");
            getParameter();
        }
        ImGui.end();
    }

    private void showMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    open();
                }
                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    save();
                }
                ImGui.endMenu();
            }

            ImGui.endMenuBar();
        }
    }

    private void loadFont() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(3);
            aFilterPatterns.put(stack.UTF8("*.ttf"));
            aFilterPatterns.put(stack.UTF8("*.ttc"));
            aFilterPatterns.put(stack.UTF8("*.otf"));
            aFilterPatterns.flip();
            String filename = TinyFileDialogs.tinyfd_openFileDialog("Open Font File", "", aFilterPatterns,
                    "Fonts (*.ttf, *.ttc, *.otf)", false);
            if (filename != null) {
                font.set(filename);

                getParameter();
            }
        }
    }

    private void getParameter() {
        if (font.getLength() == 0) {
            return;
        }

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

        int width = Integer.parseInt(packerSizes[packerWidth.get()]);
        int height = Integer.parseInt(packerSizes[packerHeight.get()]);
        packer = new Packer(Image.Format.RGBA8, width, height, packPadding.get(), false, packStrategy);

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

        parameter.setMinFilter(Texture.MinFilter.valueOf(minFilterOptions[minFilter.get()]));
        parameter.setMagFilter(Texture.MagFilter.valueOf(magFilterOptions[magFilter.get()]));

        String matDefName = matDefs[matDefId.get()];
        parameter.setMatDefName(matDefName);
        parameter.setColorMapParamName(colorMapParamName.get());
        parameter.setVertexColorParamName(vertexColorParamName.get());
        parameter.setUseVertexColor(useVertexColor.get());

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>(parameter.getMatDefName()));
        parameter.setMatDef(matDef);

        if (generator != null) {
            generator.close();
            generator = null;
        }

        if (bmfont != null) {
            bmfont = null;
        }

        generator = new FtFontGenerator(new File(font.get()), 0);

        bmfont = generator.generateFont(parameter);

        buildFtBitmapText(bmfont);
    }

    private void buildFtBitmapText(BitmapFont fnt) {
        BitmapText bitmapText = new BitmapText(fnt);
        bitmapText.setBox(new Rectangle(0, 0, 900, 720));
        bitmapText.setText(content.get());
        bitmapText.move(0, cam.getHeight(), 0);

        this.enqueue(() -> {
            if (bmtext != null) {
                bmtext.removeFromParent();
            }
            scene.attachChild(bitmapText);
            this.bmtext = bitmapText;
            logger.info("scene:{}", scene.getChildren());
        });
    }

    private void open() {
        if (loadCount.get() >= 1) {
            return;
        }
        // open *.presets file and load
        try (MemoryStack stack = stackPush()) {
            String filename = TinyFileDialogs.tinyfd_openFileDialog("Open Presets File", "", null,
                    "Presets (*.presets)", false);
            if (filename != null) {
                threadPool.submit(() -> {
                    loadCount.getAndAdd(1);
                    open(new File(filename));
                    getParameter();
                    loadCount.getAndAdd(-1);
                });
            }
        }

    }

    private void save() {
        if (loadCount.get() >= 1) {
            return;
        }

        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(3);
            aFilterPatterns.put(stack.UTF8("*.presets"));
            aFilterPatterns.flip();
            String filename = TinyFileDialogs.tinyfd_saveFileDialog("Save Presets File", "", aFilterPatterns,
                    "Presets (*.presets)");

            if (filename != null) {
                threadPool.submit(() -> {
                    loadCount.getAndAdd(1);
                    save(new File(filename));
                    loadCount.getAndAdd(-1);
                });
            }
        }
    }

    private void open(File file) {

        Properties properties = new OrderedProperties();
        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (IOException e) {
            logger.error("open presets file {} error", file, e);
            return;
        }

        setString(font, "font.file", properties);
        setIndex(packerWidth, "pack.width", properties, packerSizes);
        setIndex(packerHeight, "pack.height", properties, packerSizes);
        setInt(packPadding, "pack.padding", properties);
        setIndex(strategy, "pack.strategy", properties, strategyOptions);

        setInt(size, "font.size", properties);
        setBool(kerning, "font.kerning", properties);
        setBool(incremental, "font.incremental", properties);

        setIndex(renderMode, "render.mode", properties, renderModes);
        setRGBA(color, "render.color", properties);
        setFloat(gamma, "render.gamma", properties);
        setInt(renderCount, "render.count", properties);
        setInt(spread, "render.spread", properties);
        setIndex(hinting, "render.hinting", properties, hintings);

        setInt(borderWidth, "border.width", properties);
        setRGBA(borderColor, "border.color", properties);
        setFloat(borderGamma, "border.gamma", properties);
        setBool(borderStraight, "border.straight", properties);

        setInt(shadowOffsetX, "shadow.offsetX", properties);
        setInt(shadowOffsetY, "shadow.offsetY", properties);
        setRGBA(shadowColor, "shadow.color", properties);

        setInt(spaceX, "space.x", properties);
        setInt(spaceY, "space.y", properties);
        setInt(padLeft, "padding.left", properties);
        setInt(padRight, "padding.right", properties);
        setInt(padTop, "padding.top", properties);
        setInt(padBottom, "padding.bottom", properties);

        setIndex(minFilter, "minFilter", properties, minFilterOptions);
        setIndex(magFilter, "magFilter", properties, magFilterOptions);

        setIndex(matDefId, "material.matDefName", properties, matDefs);
        setString(colorMapParamName, "material.colorMapParamName", properties);
        setString(vertexColorParamName, "material.vertexColorParamName", properties);
        setBool(useVertexColor, "material.useVertexColor", properties);

    }

    private void setIndex(ImInt imInt, String propertyName, Properties properties, String[] options) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        imInt.set(Arrays.asList(options).indexOf(value));
    }

    private void setString(ImString imString, String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            imString.set("");
            return;
        }
        imString.set(value);
    }

    private void setInt(ImInt imInt, String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        imInt.set(Integer.parseInt(value));
    }

    private void setFloat(ImFloat imFloat, String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        imFloat.set(Float.parseFloat(value));
    }

    private void setBool(ImBoolean imBool, String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        imBool.set(Boolean.parseBoolean(value));
    }

    private void setRGBA(float[] color, String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (value.length() == 8) {
            value = "0x" + value;
        }
        int c = Integer.parseInt(value, 16);
        int red = (c >> 24) & 0xFF;
        int green = (c >> 16) & 0xFF;
        int blue = (c >> 8) & 0xFF;
        int alpha = c & 0xFF;

        color[0] = red / 255f;
        color[1] = green / 255f;
        color[2] = blue / 255f;
        color[3] = alpha / 255f;
    }

    private void save(File file) {
        // save all the parameters to a *.presets file
        Properties properties = new OrderedProperties();
        properties.setProperty("pack.width", String.valueOf(packer.getPageWidth()));
        properties.setProperty("pack.height", String.valueOf(packer.getPageHeight()));
        properties.setProperty("pack.padding", String.valueOf(packer.getPadding()));
        properties.setProperty("pack.strategy", packer.getPackStrategy().getClass().getSimpleName());

        properties.setProperty("font.file", font.get());
        properties.setProperty("font.size", String.valueOf(parameter.getSize()));
        properties.setProperty("font.kerning", String.valueOf(parameter.isKerning()));
        properties.setProperty("font.incremental", String.valueOf(parameter.isIncremental()));
        properties.setProperty("render.mode", parameter.getRenderMode().name());
        properties.setProperty("render.color", String.format("%08X", parameter.getColor().asIntRGBA()));
        properties.setProperty("render.gamma", String.valueOf(parameter.getGamma()));
        properties.setProperty("render.count", String.valueOf(parameter.getRenderCount()));
        properties.setProperty("render.spread", String.valueOf(parameter.getSpread()));
        properties.setProperty("render.hinting",parameter.getHinting().name());

        properties.setProperty("border.width", String.valueOf(parameter.getBorderWidth()));
        properties.setProperty("border.color", String.format("%08X", parameter.getBorderColor().asIntRGBA()));
        properties.setProperty("border.straight", String.valueOf(parameter.isBorderStraight()));
        properties.setProperty("border.gamma", String.valueOf(parameter.getBorderGamma()));

        properties.setProperty("shadow.offsetX", String.valueOf(parameter.getShadowOffsetX()));
        properties.setProperty("shadow.offsetY", String.valueOf(parameter.getShadowOffsetY()));
        properties.setProperty("shadow.color", String.format("%08X", parameter.getShadowColor().asIntRGBA()));

        properties.setProperty("space.x", String.valueOf(parameter.getSpaceX()));
        properties.setProperty("space.y", String.valueOf(parameter.getSpaceY()));

        properties.setProperty("padding.left", String.valueOf(parameter.getPadLeft()));
        properties.setProperty("padding.right", String.valueOf(parameter.getPadRight()));
        properties.setProperty("padding.top", String.valueOf(parameter.getPadTop()));
        properties.setProperty("padding.bottom", String.valueOf(parameter.getPadBottom()));

        properties.setProperty("texture.minFilter", parameter.getMinFilter().name());
        properties.setProperty("texture.magFilter", parameter.getMagFilter().name());

        properties.setProperty("material.matDefName", parameter.getMatDefName());
        properties.setProperty("material.colorMapParamName", parameter.getColorMapParamName());
        properties.setProperty("material.vertexColorParamName", parameter.getVertexColorParamName());
        properties.setProperty("material.useVertexColor", String.valueOf(parameter.isUseVertexColor()));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Font Presets");
            fos.flush();
        } catch (IOException e) {
            logger.error("save presets file error:{}", file, e);
        }
    }
}
