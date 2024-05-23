package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.MaterialDef;
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
import io.github.jmecn.font.utils.FileUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

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
        settings.setTitle(i18n.getString(TITLE));
        settings.setSamples(4);
        settings.setResizable(true);

        Main app = new Main(new StatsAppState(), new DetailedProfilerState());
        app.setSettings(settings);
        app.start();
    }

    static Logger logger = LoggerFactory.getLogger(Main.class);

    static I18n i18n = new I18n();

    private final Node scene;
    private Rectangle rectangle;
    private BitmapFont bmfont;
    private BitmapText bmtext;

    private File presetFile;// current preset file
    private Packer packer;
    private FtFontGenerator generator;
    private final FtFontParameter parameter;

    ImString font = new ImString();

    // here are menu
    ImBoolean showPages = new ImBoolean();
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
            packerWidth.set(indexOf(PACKER_SIZE_OPTIONS, String.valueOf(packer.getPageWidth())));
            packerHeight.set(indexOf(PACKER_SIZE_OPTIONS, String.valueOf(packer.getPageHeight())));
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

        // read all bytes
        AssetInfo assetInfo = assetManager.locateAsset(new AssetKey<>("font/NotoSerifSC-Regular.otf"));
        byte[] data = FileUtils.readAllBytes(assetInfo.openStream());

        ImFont imFont = io.getFonts().addFontFromMemoryTTF(data, 16, imFontConfig, glyphRanges);
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

        if (showPages.get()) {
            showImagesWindow(showPages);
        }

        if (showText.get()) {
            showTextWindow(showText);
        }

        // End the ImGui frame
        ImGuiJme3.endFrame();
    }

    private void showParameterWindow() {
        ImGui.begin(i18n.getString(MAIN_TITLE), ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.AlwaysAutoResize);

        int colorPickerFlags = ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.AlphaBar;
        boolean parameterChanged = false;

        //////// menu bar /////////
        showMenu();
        ///////////////////////////

        ImGui.pushItemWidth(200);
        ImGui.inputTextWithHint("##font", i18n.getString(FONT_FILE), font, ImGuiInputTextFlags.ReadOnly);
        ImGui.popItemWidth();

        if (ImGui.collapsingHeader(i18n.getString(FONT_TITLE))) {
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.dragScalar(i18n.getString(FONT_SIZE), ImGuiDataType.S32, size, 0.2f, MIN_FONT_SIZE, MAX_FONT_SIZE, "%d", ImGuiSliderFlags.AlwaysClamp);
            ImGui.popItemWidth();
            ImGui.sameLine();
            ImGui.pushButtonRepeat(true);
            if (ImGui.arrowButton("##size.left", ImGuiDir.Left)) {
                size.set(Math.max(size.get() - 1, MIN_FONT_SIZE));
                parameterChanged = true;
            }
            ImGui.sameLine(0, ImGui.getStyle().getItemInnerSpacingX());
            if (ImGui.arrowButton("##size.right", ImGuiDir.Right)) {
                size.set(Math.min(size.get() + 1, MAX_FONT_SIZE));
                parameterChanged = true;
            }
            ImGui.popButtonRepeat();

            parameterChanged |= ImGui.checkbox(i18n.getString(FONT_KERNING), kerning);
            parameterChanged |= ImGui.checkbox(i18n.getString(FONT_INCREMENTAL), incremental);
        }

        if (ImGui.collapsingHeader(i18n.getString(RENDER_TITLE))) {

            ImGui.pushItemWidth(80);
            parameterChanged |= ImGui.combo(i18n.getString(RENDER_MODE), renderMode, RENDER_MODE_OPTIONS);
            if (renderMode.get() == RenderMode.SDF.ordinal()) {
                parameterChanged |= ImGui.dragScalar(i18n.getString(RENDER_SPREAD), ImGuiDataType.S32, spread, 0.2f, FtLibrary.MIN_SPREAD, FtLibrary.MAX_SPREAD, "%d", ImGuiSliderFlags.AlwaysClamp);
            }
            parameterChanged |= ImGui.combo(i18n.getString(RENDER_HINTING), hinting, HINTING_OPTIONS);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString(RENDER_COLOR), color, colorPickerFlags);
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.inputFloat(i18n.getString(RENDER_GAMMA), gamma, 0.1f);
            parameterChanged |= ImGui.dragScalar(i18n.getString(RENDER_COUNT), ImGuiDataType.S32, renderCount, 0.1f, 1, 4, "%d", ImGuiSliderFlags.AlwaysClamp);
            ImGui.popItemWidth();
        }

        if (ImGui.collapsingHeader(i18n.getString(MATERIAL_TITLE))) {
            ImGui.pushItemWidth(160);
            parameterChanged |= ImGui.combo(i18n.getString(MATERIAL_DEFINE), matDefId, MAT_DEF_OPTIONS);
            ImGui.popItemWidth();
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.inputText(i18n.getString(MATERIAL_COLOR_MAP), colorMapParamName);
            parameterChanged |= ImGui.inputText(i18n.getString(MATERIAL_VERTEX_COLOR), vertexColorParamName);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.checkbox(i18n.getString(MATERIAL_USE_VERTEX_COLOR), useVertexColor);

            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.combo(i18n.getString(TEXTURE_MIN_FILTER), minFilter, MIN_FILTER_OPTIONS);
            parameterChanged |= ImGui.combo(i18n.getString(TEXTURE_MAG_FILTER), magFilter, MAG_FILTER_OPTIONS);
            ImGui.popItemWidth();
        }

        if (ImGui.collapsingHeader(i18n.getString(BORDER_TITLE))) {
            ImGui.pushItemWidth(100);
            parameterChanged |= ImGui.sliderInt(i18n.getString(BORDER_WIDTH), borderWidth.getData(), 0, 10);
            parameterChanged |= ImGui.inputFloat(i18n.getString(BORDER_GAMMA), borderGamma, 0.1f);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString(BORDER_COLOR), borderColor, colorPickerFlags);
            parameterChanged |= ImGui.checkbox(i18n.getString(BORDER_STRAIGHT), borderStraight);
        }

        if (ImGui.collapsingHeader(i18n.getString(SHADOW_TITLE))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString(SHADOW_OFFSET_X), shadowOffsetX.getData(), -10, 10);
            parameterChanged |= ImGui.sliderInt(i18n.getString(SHADOW_OFFSET_Y), shadowOffsetY.getData(), -10, 10);
            ImGui.popItemWidth();
            parameterChanged |= ImGui.colorEdit4(i18n.getString(SHADOW_COLOR), shadowColor, colorPickerFlags);
        }

        if (ImGui.collapsingHeader(i18n.getString(SPACE_TITLE))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString(SPACE_X), spaceX.getData(), 0, 100);
            parameterChanged |= ImGui.sliderInt(i18n.getString(SPACE_Y), spaceY.getData(), 0, 100);
            ImGui.popItemWidth();
        }

        if (ImGui.collapsingHeader(i18n.getString(PADDING_TITLE))) {
            ImGui.indent(50);
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.sliderInt(i18n.getString(PADDING_TOP), padTop.getData(), 0, 10);
            ImGui.indent(-50);
            parameterChanged |= ImGui.sliderInt(i18n.getString(PADDING_LEFT), padLeft.getData(), 0, 10);
            ImGui.sameLine();
            parameterChanged |= ImGui.sliderInt(i18n.getString(PADDING_RIGHT), padRight.getData(), 0, 10);
            ImGui.indent(50);
            parameterChanged |= ImGui.sliderInt(i18n.getString(PADDING_BOTTOM), padBottom.getData(), 0, 10);
            ImGui.indent(-50);
        }

        if (ImGui.collapsingHeader(i18n.getString(PACK_TITLE))) {
            ImGui.pushItemWidth(60);
            parameterChanged |= ImGui.combo(i18n.getString(PACK_WIDTH), packerWidth, PACKER_SIZE_OPTIONS);
            parameterChanged |= ImGui.combo(i18n.getString(PACK_HEIGHT), packerHeight, PACKER_SIZE_OPTIONS);
            parameterChanged |= ImGui.dragInt(i18n.getString(PACK_PADDING), packPadding.getData(), 1f, 0f, 100f);
            ImGui.popItemWidth();
            ImGui.pushItemWidth(120);
            parameterChanged |= ImGui.combo(i18n.getString(PACK_STRATEGY), strategy, STRATEGY_OPTIONS);
        }
        if (parameterChanged) {
            refreshParameter();
        }
        ImGui.end();
    }

    private void showMenu() {
        if (ImGui.beginMenuBar()) {
            showFileMenu();

            if (ImGui.beginMenu(i18n.getString(MENU_VIEW))) {
                ImGui.menuItem(i18n.getString(MENU_VIEW_PAGES), "F2", showPages);
                ImGui.menuItem(i18n.getString(MENU_VIEW_TEXT), "F3", showText);
                ImGui.endMenu();
            }

            if (ImGui.beginMenu(i18n.getString(MENU_LANGUAGE))) {
                for (Locale locale : I18n.SUPPORTED) {
                    if (ImGui.menuItem(locale.getDisplayName(locale))) {
                        i18n.setLocale(locale);
                        this.getContext().setTitle(i18n.getString(TITLE));
                    }
                }
                ImGui.endMenu();
            }
            ImGui.endMenuBar();
        }
    }

    private void showFileMenu() {
        if (ImGui.beginMenu(i18n.getString(MENU_FILE))) {
            if (ImGui.menuItem(i18n.getString(MENU_FILE_LOAD), "Ctrl+F")) {
                loadFont();
            }
            if (ImGui.menuItem(i18n.getString(MENU_FILE_OPEN), "Ctrl+O")) {
                open();
            }
            if (ImGui.menuItem(i18n.getString(MENU_FILE_SAVE), "Ctrl+S")) {
                if (presetFile != null) {
                    save(presetFile);
                } else {
                    saveAs();
                }
            }
            if (ImGui.menuItem(i18n.getString(MENU_FILE_SAVE_AS))) {
                saveAs();
            }
            ImGui.endMenu();
        }
    }

    private void showImagesWindow(ImBoolean open) {
        if (!ImGui.begin(i18n.getString(IMAGES_TITLE), open, ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.AlwaysUseWindowPadding)) {
            ImGui.end();
            return;
        }

        if (packer != null && (ImGui.beginTabBar("#images", ImGuiTabBarFlags.None))) {
            int i = 0;
            for (Page page : packer.getPages()) {
                if (ImGui.beginTabItem("#" + i++)) {
                    ImGui.image(page.getImage().getId(), page.getImage().getWidth(), page.getImage().getHeight(), 0f, 1f, 1f, 0f);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        } else {
            ImGui.text(i18n.getString(IMAGES_EMPTY));
        }

        ImGui.end();
    }

    private void showTextWindow(ImBoolean open) {
        if (!ImGui.begin(i18n.getString(TEXT_TITLE), open, ImGuiWindowFlags.AlwaysVerticalScrollbar | ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.end();
            return;
        }

        ImGui.inputTextMultiline("##text", content, 400, 200, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.CallbackEdit | ImGuiInputTextFlags.AllowTabInput);
        ImGui.end();
    }

    private void loadFont() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(3);
            aFilterPatterns.put(stack.UTF8("*.ttf"));
            aFilterPatterns.put(stack.UTF8("*.ttc"));
            aFilterPatterns.put(stack.UTF8("*.otf"));
            aFilterPatterns.flip();
            String filename = TinyFileDialogs.tinyfd_openFileDialog(i18n.getString(DIALOG_FONT_OPEN), "", aFilterPatterns,
                    i18n.getString(DIALOG_FONT_DESC), false);
            if (filename != null) {
                refreshGenerator(filename);
                refreshParameter();
            }
        }
    }

    private void refreshGenerator(String filename) {
        if (filename != null && !filename.isEmpty()) {
            if (filename.equals(font.get())) {
                return;// not changed
            }
            font.set(filename);

            File fontFile = new File(filename);
            if (generator != null) {
                generator.close();
                generator = null;
            }

            generator = new FtFontGenerator(fontFile, 0);
        }
    }

    private void refreshParameter() {
        if (generator == null) {
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

        if (bmfont != null) {
            bmfont = null;
        }

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
        });
    }

    @Override
    public void reshape(int w, int h) {
        rectangle = new Rectangle(0, 0, w, h);
        if (bmtext != null) {
            bmtext.setBox(rectangle);
            bmtext.setLocalTranslation(0, h, 0);
        }

        super.reshape(w, h);
    }

    private void open() {
        // open *.properties file and load
        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(1);
            aFilterPatterns.put(stack.UTF8("*.properties"));
            aFilterPatterns.flip();
            String filename = TinyFileDialogs.tinyfd_openFileDialog(i18n.getString(DIALOG_PRESET_OPEN), "",
                    aFilterPatterns, i18n.getString(DIALOG_PRESET_DESC), false);
            if (filename != null) {
                File file = new File(filename);
                try {
                    open(file);
                    refreshParameter();
                    presetFile = file;
                } catch (Exception e) {
                    logger.error("open file failed", e);
                }
            }
        }

    }

    private void saveAs() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(1);
            aFilterPatterns.put(stack.UTF8("*.properties"));
            aFilterPatterns.flip();
            String filename = TinyFileDialogs.tinyfd_saveFileDialog(i18n.getString(DIALOG_PRESET_SAVE), "",
                    aFilterPatterns, i18n.getString(DIALOG_PRESET_DESC));
            if (filename != null) {
                save(new File(filename));
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

        String fontFilename = properties.getProperty(FONT_FILE);
        refreshGenerator(fontFilename);
        font.set(fontFilename);

        setIndex(packerWidth, PACK_WIDTH, properties, PACKER_SIZE_OPTIONS);
        setIndex(packerHeight, PACK_HEIGHT, properties, PACKER_SIZE_OPTIONS);
        setInt(packPadding, PACK_PADDING, properties);
        setIndex(strategy, PACK_STRATEGY, properties, STRATEGY_OPTIONS);

        setInt(size, FONT_SIZE, properties);
        setBool(kerning, FONT_KERNING, properties);
        setBool(incremental, FONT_INCREMENTAL, properties);

        setIndex(hinting, RENDER_HINTING, properties, HINTING_OPTIONS);
        setIndex(renderMode, RENDER_MODE, properties, RENDER_MODE_OPTIONS);
        setInt(spread, RENDER_SPREAD, properties);
        setRGBA(color, RENDER_COLOR, properties);
        setFloat(gamma, RENDER_GAMMA, properties);
        setInt(renderCount, RENDER_COUNT, properties);

        setInt(borderWidth, BORDER_WIDTH, properties);
        setRGBA(borderColor, BORDER_COLOR, properties);
        setFloat(borderGamma, BORDER_GAMMA, properties);
        setBool(borderStraight, BORDER_STRAIGHT, properties);

        setInt(shadowOffsetX, SHADOW_OFFSET_X, properties);
        setInt(shadowOffsetY, SHADOW_OFFSET_Y, properties);
        setRGBA(shadowColor, SHADOW_COLOR, properties);

        setInt(spaceX, SPACE_X, properties);
        setInt(spaceY, SPACE_Y, properties);

        setInt(padLeft, PADDING_LEFT, properties);
        setInt(padRight, PADDING_RIGHT, properties);
        setInt(padTop, PADDING_TOP, properties);
        setInt(padBottom, PADDING_BOTTOM, properties);

        setIndex(minFilter, TEXTURE_MIN_FILTER, properties, MIN_FILTER_OPTIONS);
        setIndex(magFilter, TEXTURE_MAG_FILTER, properties, MAG_FILTER_OPTIONS);

        setIndex(matDefId, MATERIAL_DEFINE, properties, MAT_DEF_OPTIONS);
        setString(colorMapParamName, MATERIAL_COLOR_MAP, properties);
        setString(vertexColorParamName, MATERIAL_VERTEX_COLOR, properties);
        setBool(useVertexColor, MATERIAL_USE_VERTEX_COLOR, properties);
    }

    private int indexOf(String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
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
        properties.setProperty(PACK_WIDTH, String.valueOf(packer.getPageWidth()));
        properties.setProperty(PACK_HEIGHT, String.valueOf(packer.getPageHeight()));
        properties.setProperty(PACK_PADDING, String.valueOf(packer.getPadding()));
        properties.setProperty(PACK_STRATEGY, packer.getPackStrategy().getClass().getSimpleName());

        properties.setProperty(Constant.FONT_FILE, font.get());
        properties.setProperty(FONT_SIZE, String.valueOf(parameter.getSize()));
        properties.setProperty(FONT_KERNING, String.valueOf(parameter.isKerning()));
        properties.setProperty(FONT_INCREMENTAL, String.valueOf(parameter.isIncremental()));

        properties.setProperty(RENDER_HINTING,parameter.getHinting().name());
        properties.setProperty(RENDER_MODE, parameter.getRenderMode().name());
        properties.setProperty(RENDER_SPREAD, String.valueOf(parameter.getSpread()));
        properties.setProperty(RENDER_COLOR, String.format("%08X", parameter.getColor().asIntRGBA()));
        properties.setProperty(RENDER_GAMMA, String.valueOf(parameter.getGamma()));
        properties.setProperty(RENDER_COUNT, String.valueOf(parameter.getRenderCount()));

        properties.setProperty(BORDER_WIDTH, String.valueOf(parameter.getBorderWidth()));
        properties.setProperty(BORDER_COLOR, String.format("%08X", parameter.getBorderColor().asIntRGBA()));
        properties.setProperty(BORDER_GAMMA, String.valueOf(parameter.getBorderGamma()));
        properties.setProperty(BORDER_STRAIGHT, String.valueOf(parameter.isBorderStraight()));

        properties.setProperty(SHADOW_OFFSET_X, String.valueOf(parameter.getShadowOffsetX()));
        properties.setProperty(SHADOW_OFFSET_Y, String.valueOf(parameter.getShadowOffsetY()));
        properties.setProperty(SHADOW_COLOR, String.format("%08X", parameter.getShadowColor().asIntRGBA()));

        properties.setProperty(SPACE_X, String.valueOf(parameter.getSpaceX()));
        properties.setProperty(SPACE_Y, String.valueOf(parameter.getSpaceY()));

        properties.setProperty(PADDING_LEFT, String.valueOf(parameter.getPadLeft()));
        properties.setProperty(PADDING_RIGHT, String.valueOf(parameter.getPadRight()));
        properties.setProperty(PADDING_TOP, String.valueOf(parameter.getPadTop()));
        properties.setProperty(PADDING_BOTTOM, String.valueOf(parameter.getPadBottom()));

        properties.setProperty(TEXTURE_MIN_FILTER, parameter.getMinFilter().name());
        properties.setProperty(TEXTURE_MAG_FILTER, parameter.getMagFilter().name());

        properties.setProperty(MATERIAL_DEFINE, parameter.getMatDefName());
        properties.setProperty(MATERIAL_COLOR_MAP, parameter.getColorMapParamName());
        properties.setProperty(MATERIAL_VERTEX_COLOR, parameter.getVertexColorParamName());
        properties.setProperty(MATERIAL_USE_VERTEX_COLOR, String.valueOf(parameter.isUseVertexColor()));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Font Presets");
            fos.flush();
        } catch (IOException e) {
            logger.error("save presets file error:{}", file, e);
        }
    }

}
