package io.github.jmecn.font.editor.app;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

/**
 * 灯光模块
 * 
 * @author yanmoayuan
 *
 */
public class LightState extends BaseAppState {

    private Node rootNode;

    // 光源
    private AmbientLight al;
    private DirectionalLight dl;

    @Override
    protected void initialize(Application app) {

        ViewPort viewPort = app.getViewPort();
        viewPort.setBackgroundColor(new ColorRGBA(0.75f, 0.8f, 0.9f, 1f));

        AssetManager assetManager = app.getAssetManager();

        rootNode = ((SimpleApplication) app).getRootNode();

        /*
         * 光源
         */
        // 环境光
        al = new AmbientLight(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));

        // 点光源
        Vector3f position = new Vector3f(-4, -10, -5).normalizeLocal();
        dl = new DirectionalLight(position, new ColorRGBA(0.7f, 0.7f, 0.7f, 0.7f));

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 1024, 4);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
        dlsr.setShadowIntensity(0.3f);
        dlsr.setLight(dl);
        dlsr.setRenderBackFacesShadows(false);

        app.getViewPort().addProcessor(dlsr);

    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {
        // 添加光源
        rootNode.addLight(al);
        rootNode.addLight(dl);
    }

    @Override
    protected void onDisable() {
        // 移除光源
        rootNode.removeLight(al);
        rootNode.removeLight(dl);
    }

}
