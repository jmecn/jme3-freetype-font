package io.github.jmecn.font.editor;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import io.github.jmecn.font.editor.app.CheckerBoardState;
import io.github.jmecn.font.editor.app.LightState;
import io.github.jmecn.font.editor.ui.window.SimpleWindowManager;
import io.github.jmecn.font.plugins.FtFontKey;
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

    public Main(AppState... states) {
        super(states);
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

        ///// init lemur
        BitmapFont font = assetManager.loadAsset(new FtFontKey("Font/unifont-15.1.05.otf", 16, true));

        GuiGlobals.initialize(this);
        BaseStyles.loadStyleResources("ui/style/style.groovy");
        GuiGlobals.getInstance().getStyles().setDefaultStyle("dark");
        GuiGlobals.getInstance().getStyles().setDefault(font);

        ///// init app state /////
        stateManager.attach(new LightState());
        stateManager.attach(new CheckerBoardState());
        stateManager.attach(new SimpleWindowManager());

        // init camera
        cam.setLocation(new Vector3f(0f, 3f, 10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.setFov(60);
    }
}
