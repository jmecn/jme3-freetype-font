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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import imgui.*;
import imgui.flag.*;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.jmecn.font.CommonChars;
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
import java.util.Properties;
import java.util.ResourceBundle;

import static org.lwjgl.system.MemoryStack.stackPush;
import static io.github.jmecn.font.editor.Constant.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Main extends SimpleApplication {
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(Constant.WIDTH, Constant.HEIGHT);
        settings.setTitle(i18n.getString("title"));
        settings.setSamples(4);
        settings.setResizable(true);

        Main app = new Main(new StatsAppState(), new DetailedProfilerState());
        app.setSettings(settings);
        app.start();
    }

    static Logger logger = LoggerFactory.getLogger(Main.class);

    static ResourceBundle i18n = ResourceBundle.getBundle("io.github.jmecn.font.editor/lang");

    private final Node scene;
    private Rectangle rectangle;

    /////////////// bitmapfont bitmaptext ////////////////
    private File fontFile;// current font file
    private File presetFile;// current preset file
    private Packer packer;
    private FtFontGenerator generator;
    private final FtFontParameter parameter;
    private BitmapFont bmfont;
    private BitmapText bmtext;

    ImString font = new ImString();

    // here are menu
    ImBoolean showImages = new ImBoolean();
    ImBoolean showText = new ImBoolean();

    // here is all the FtFontParameters, for imgui
    ImInt size = new ImInt();
    ImInt renderMode = new ImInt();

    ImInt spread = new ImInt();
    ImInt hinting = new ImInt();
    ImBoolean kerning = new ImBoolean();
    ImBoolean incremental = new ImBoolean();
    ImString content = new ImString(Constant.TEXT);

    // packer
    ImInt packerWidth = new ImInt();
    ImInt packerHeight = new ImInt();
    ImInt packPadding = new ImInt();
    ImInt strategy = new ImInt();// 0 - SkylineStrategy, 1 -

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
    ImInt magFilter = new ImInt();

    // materials
    ImInt matDefId = new ImInt();
    ImString colorMapParamName = new ImString();
    ImString vertexColorParamName = new ImString();
    ImBoolean useVertexColor = new ImBoolean();


    public Main(AppState... states) {
        super(states);
        scene = new Node("freetype-font");

        parameter = new FtFontParameter();
        rectangle = new Rectangle(0, 0, WIDTH, HEIGHT);
    }

    /**
     * set parameter to imgui
     */
    private void setParameter() {
        size.set(parameter.getSize());

        // packer
        if (packer != null) {
            packerWidth.set(indexOf(PACKER_SIZE_OPTIONS, packer.getPageWidth()));
            packerHeight.set(indexOf(PACKER_SIZE_OPTIONS, packer.getPageHeight()));
            packPadding.set(packer.getPadding());
            strategy.set(indexOf(STRATEGY_OPTIONS, packer.getPackStrategy().getClass().getSimpleName()));
        } else {
            packerWidth.set(1);
            packerHeight.set(1);
            packPadding.set(1);
            strategy.set(1);
        }

        renderMode.set(parameter.getRenderMode().ordinal());
        spread.set(parameter.getSpread());
        hinting.set(parameter.getHinting().ordinal());
        kerning.set(parameter.isKerning());
        incremental.set(parameter.isIncremental());

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

        matDefId.set(indexOf(MAT_DEF_OPTIONS, parameter.getMatDefName()));
        colorMapParamName.set(parameter.getColorMapParamName());
        vertexColorParamName.set(parameter.getVertexColorParamName());
        useVertexColor.set(parameter.isUseVertexColor());
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(FtFontLoader.class, "otf", "ttf");

        guiNode.attachChild(scene);

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

        // init camera
        cam.setLocation(new Vector3f(0f, 3f, 20f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFov(60);
    }


    private void initImGui() {
        ImGuiJme3.initialize(this);

        ImGui.getIO().setIniFilename(null);
        // Load custom font
        ImGuiIO io = ImGui.getIO();

        io.getFonts().addFontDefault(); // Add default font for latin glyphs

        // You can use the ImFontGlyphRangesBuilder helper to create glyph ranges based on text input.
        // For example: for a game where your script is known, if you can feed your entire script to it (using addText) and only build the characters the game needs.
        // Here we are using it just to combine all required glyphs in one place
        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();          // Glyphs ranges provide
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addText(CommonChars.SIMPLIFIED_CHINESE.getChars());

        // Font config for custom fonts
        ImFontConfig imFontConfig = new ImFontConfig();
        imFontConfig.setSizePixels(12);
        imFontConfig.setMergeMode(true);      // Merge Default, Cyrillic, Japanese ranges and manual specific chars

        final short[] glyphRanges = rangesBuilder.buildRanges();
        ImFont imFont = io.getFonts().addFontFromFileTTF("font/NotoSerifSC-Regular.otf", 16, imFontConfig, glyphRanges);
        io.getFonts().build();           // Build custom font
        io.setFontDefault(imFont);       // Set custom font to default

        ImGuiJme3.refreshFontTexture();        // Don't forget to refresh the font texture!

        imFontConfig.destroy();               // Destroy the font config
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

        showParameterWindow();

        if (showImages.get()) {
            showImagesWindow(showImages);
        }

        if (showText.get()) {
            showTextWindow(showText);
        }

        // End the ImGui frame
        ImGuiJme3.endFrame();
    }

    private void showParameterWindow() {
        ImGui.begin(i18n.getString("main.title"), ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.AlwaysAutoResize);

        int colorPickerFlags = ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.AlphaBar;
        boolean parameterChanged = false;

        //////// menu bar /////////
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu(i18n.getString("menu.file"))) {
                if (ImGui.menuItem(i18n.getString("menu.file.load"), "Ctrl+F")) {
                    loadFont();
                }
                if (ImGui.menuItem(i18n.getString("menu.file.open"), "Ctrl+O")) {
                    open();
                }
                if (ImGui.menuItem(i18n.getString("menu.file.save"), "Ctrl+S", false, presetFile != null)) {
                    save(presetFile);
                }
                if (ImGui.menuItem(i18n.getString("menu.file.saveAs"))) {
                    saveAs();
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu(i18n.getString("menu.view"))) {
                ImGui.menuItem(i18n.getString("menu.view.pages"), "F2", showImages);
                ImGui.menuItem(i18n.getString("menu.view.text"), "F3", showText);
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }
        ///////////////////////////

        ImGui.pushItemWidth(200);
        ImGui.inputTextWithHint("##font", i18n.getString("font.file"), font, ImGuiInputTextFlags.ReadOnly);
        ImGui.popItemWidth();

        if (ImGui.collapsingHeader(i18n.getString("font.title"))) {
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.dragScalar(i18n.getString("font.size"), ImGuiDataType.S32, size, 0.2f, MIN_FONT_SIZE, MAX_FONT_SIZE, "%d", ImGuiSliderFlags.AlwaysClamp | ImGuiSliderFlags.NoInput);
            ImGui.popItemWidth();

            parameterChanged |= ImGui.checkbox(i18n.getString("font.kerning"), kerning);
            parameterChanged |= ImGui.checkbox(i18n.getString("font.incremental"), incremental);
        }

        if (ImGui.collapsingHeader(i18n.getString("packer.title"))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.combo(i18n.getString("packer.width"), packerWidth, PACKER_SIZE_OPTIONS);
            parameterChanged |= ImGui.combo(i18n.getString("packer.height"), packerHeight, PACKER_SIZE_OPTIONS);
            parameterChanged |= ImGui.dragInt(i18n.getString("packer.padding"), packPadding.getData(), 1f, 0f, 100f);
            ImGui.popItemWidth();
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.combo(i18n.getString("packer.strategy"), strategy, STRATEGY_OPTIONS);
        }

        if (ImGui.collapsingHeader(i18n.getString("render.title"))) {

            ImGui.pushItemWidth(80);
            parameterChanged |= ImGui.combo(i18n.getString("render.mode"), renderMode, RENDER_MODE_OPTIONS);
            if (renderMode.get() == RenderMode.SDF.ordinal()) {
                parameterChanged |= ImGui.sliderInt(i18n.getString("render.spread"), spread.getData(), FtLibrary.MIN_SPREAD, FtLibrary.MAX_SPREAD);
            }
            parameterChanged |= ImGui.combo(i18n.getString("render.hinting"), hinting, HINTING_OPTIONS);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString("render.color"), color, colorPickerFlags);
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.inputFloat(i18n.getString("render.gamma"), gamma, 0.1f);
            parameterChanged |= ImGui.sliderInt(i18n.getString("render.count"), renderCount.getData(), 1, 10);
            ImGui.popItemWidth();
        }

        if (ImGui.collapsingHeader(i18n.getString("material.title"))) {
            ImGui.pushItemWidth(160);
            parameterChanged |= ImGui.combo(i18n.getString("material.matDefName"), matDefId, MAT_DEF_OPTIONS);
            ImGui.popItemWidth();
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.inputText(i18n.getString("material.colorMapParamName"), colorMapParamName);
            parameterChanged |= ImGui.inputText(i18n.getString("material.vertexColorParamName"), vertexColorParamName);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.checkbox(i18n.getString("material.useVertexColor"), useVertexColor);
        }

        if (ImGui.collapsingHeader(i18n.getString("border.title"))) {
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.sliderInt(i18n.getString("border.width"), borderWidth.getData(), 0, 10);
            parameterChanged |= ImGui.inputFloat(i18n.getString("border.gamma"), borderGamma, 0.1f);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString("border.color"), borderColor, colorPickerFlags);
            parameterChanged |= ImGui.checkbox(i18n.getString("border.straight"), borderStraight);
        }

        if (ImGui.collapsingHeader(i18n.getString("shadow.title"))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString("shadow.offsetX"), shadowOffsetX.getData(), -10, 10);
            parameterChanged |= ImGui.sliderInt(i18n.getString("shadow.offsetY"), shadowOffsetY.getData(), -10, 10);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString("shadow.color"), shadowColor, colorPickerFlags);
        }

        if (ImGui.collapsingHeader(i18n.getString("space.title"))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString("space.x"), spaceX.getData(), 0, 100);
            parameterChanged |= ImGui.sliderInt(i18n.getString("space.y"), spaceY.getData(), 0, 100);
            ImGui.popItemWidth();
        }

        if (ImGui.collapsingHeader(i18n.getString("padding.title"))) {
            ImGui.indent(50);
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString("padding.top"), padTop.getData(), 0, 10);
            ImGui.indent(-50);
            parameterChanged |= ImGui.sliderInt(i18n.getString("padding.left"), padLeft.getData(), 0, 10);
            ImGui.sameLine();
            parameterChanged |= ImGui.sliderInt(i18n.getString("padding.right"), padRight.getData(), 0, 10);
            ImGui.indent(50);
            parameterChanged |= ImGui.sliderInt(i18n.getString("padding.bottom"), padBottom.getData(), 0, 10);
            ImGui.indent(-50);
        }

        if (ImGui.collapsingHeader(i18n.getString("texture.title"))) {
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.combo(i18n.getString("texture.minFilter"), minFilter, MIN_FILTER_OPTIONS);
            parameterChanged |= ImGui.combo(i18n.getString("texture.magFilter"), magFilter, MAG_FILTER_OPTIONS);
            ImGui.popItemWidth();
        }

        if (parameterChanged) {
            getParameter();
        }
        ImGui.end();
    }

    private void showImagesWindow(ImBoolean open) {
        if (!ImGui.begin(i18n.getString("images.title"), open, ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.AlwaysUseWindowPadding)) {
            ImGui.end();
            return;
        }

        if (packer != null && (ImGui.beginTabBar("#images", ImGuiTabBarFlags.None))) {
            int i = 0;
            for (Page page : packer.getPages()) {
                if (ImGui.beginTabItem("image#" + i++)) {
                    ImGui.image(page.getImage().getId(), page.getImage().getWidth(), page.getImage().getHeight(), 0f, 1f, 1f, 0f);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        } else {
            ImGui.text(i18n.getString("images.empty"));
        }

        ImGui.end();
    }

    private void showTextWindow(ImBoolean open) {
        if (!ImGui.begin(i18n.getString("text.title"), open, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.end();
            return;
        }

        ImGui.inputTextMultiline("##text", content, 400, 100, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.CallbackEdit);
        ImGui.end();
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

        int width = Integer.parseInt(PACKER_SIZE_OPTIONS[packerWidth.get()]);
        int height = Integer.parseInt(PACKER_SIZE_OPTIONS[packerHeight.get()]);
        packer = new Packer(Image.Format.RGBA8, width, height, packPadding.get(), false, packStrategy);

        parameter.setPacker(packer);
        parameter.setSize(size.get());
        parameter.setRenderMode(RenderMode.valueOf(RENDER_MODE_OPTIONS[renderMode.get()]));
        parameter.setColor(new ColorRGBA(color[0], color[1], color[2], color[3]));
        parameter.setGamma(gamma.get());
        parameter.setRenderCount(renderCount.get());
        parameter.setSpread(spread.get());
        parameter.setHinting(Hinting.valueOf(HINTING_OPTIONS[hinting.get()]));
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

        parameter.setMinFilter(Texture.MinFilter.valueOf(MIN_FILTER_OPTIONS[minFilter.get()]));
        parameter.setMagFilter(Texture.MagFilter.valueOf(MAG_FILTER_OPTIONS[magFilter.get()]));

        String matDefName = MAT_DEF_OPTIONS[matDefId.get()];
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
        bitmapText.setBox(rectangle);
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

    @Override
    public void reshape(int w, int h) {
        rectangle = new Rectangle(0, 0, w, h);
        if (bmtext != null) {
            bmtext.setBox(rectangle);
            bmtext.setLocalTranslation(0, h, 0);
            logger.info("local:{}, world:{}", bmtext.getLocalTranslation(), bmtext.getWorldTranslation());
        }

        super.reshape(w, h);
    }

    private void open() {
        // open *.presets file and load
        String filename = TinyFileDialogs.tinyfd_openFileDialog("Open Font Properties", "", null,
                "Presets (*.properties)", false);
        if (filename != null) {
            File file = new File(filename);
            try {
                open(file);
                getParameter();
                presetFile = file;
            } catch (Exception e) {
                logger.error("open file failed", e);
            }
        }

    }

    private void saveAs() {
        String filename = TinyFileDialogs.tinyfd_saveFileDialog("Save Font Properties", "", null, "Presets (*.properties)");
        if (filename != null) {
            save(new File(filename));
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
        setIndex(packerWidth, "pack.width", properties, PACKER_SIZE_OPTIONS);
        setIndex(packerHeight, "pack.height", properties, PACKER_SIZE_OPTIONS);
        setInt(packPadding, "pack.padding", properties);
        setIndex(strategy, "pack.strategy", properties, STRATEGY_OPTIONS);

        setInt(size, "font.size", properties);
        setBool(kerning, "font.kerning", properties);
        setBool(incremental, "font.incremental", properties);

        setIndex(renderMode, "render.mode", properties, RENDER_MODE_OPTIONS);
        setRGBA(color, "render.color", properties);
        setFloat(gamma, "render.gamma", properties);
        setInt(renderCount, "render.count", properties);
        setInt(spread, "render.spread", properties);
        setIndex(hinting, "render.hinting", properties, HINTING_OPTIONS);

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

        setIndex(minFilter, "minFilter", properties, MIN_FILTER_OPTIONS);
        setIndex(magFilter, "magFilter", properties, MAG_FILTER_OPTIONS);

        setIndex(matDefId, "material.matDefName", properties, MAT_DEF_OPTIONS);
        setString(colorMapParamName, "material.colorMapParamName", properties);
        setString(vertexColorParamName, "material.vertexColorParamName", properties);
        setBool(useVertexColor, "material.useVertexColor", properties);

    }

    private int indexOf(String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOf(String[] options, Object value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(String.valueOf(value))) {
                return i;
            }
        }
        return 0;
    }

    private void setIndex(ImInt imInt, String propertyName, Properties properties, String[] options) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        imInt.set(indexOf(options, value));
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
        int c = (int) Long.parseLong(value, 16);
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
