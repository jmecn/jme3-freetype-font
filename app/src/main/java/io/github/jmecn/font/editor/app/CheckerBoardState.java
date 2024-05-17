package io.github.jmecn.font.editor.app;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class CheckerBoardState extends BaseAppState implements ActionListener {

    public static final String TOGGLE_AXIS = "toggle_axis";

    private final Node rootNode = new Node("CheckerBoard");

    private AssetManager assetManager;

    @Override
    protected void initialize(Application app) {
        assetManager = app.getAssetManager();

        createCheckerBoard();

        toggleAxis();
    }

    @Override
    protected void cleanup(Application app) { /* TODO document why this method is empty */ }

    @Override
    protected void onEnable() {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        simpleApp.getRootNode().attachChild(rootNode);
        
        // 注册按键
        InputManager inputManager = getApplication().getInputManager();
        inputManager.addMapping(TOGGLE_AXIS, new KeyTrigger(KeyInput.KEY_F4));
        inputManager.addListener(this, TOGGLE_AXIS);
    }

    @Override
    protected void onDisable() {
        rootNode.removeFromParent();
        
        // 移除按键
        InputManager inputManager = getApplication().getInputManager();
        inputManager.removeListener(this);
        inputManager.deleteMapping(TOGGLE_AXIS);
    }

    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals(TOGGLE_AXIS) && keyPressed) {
            toggleAxis();
        }
    }
    
    /**
     * 坐标轴开/关
     */
    public void toggleAxis() {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        if (simpleApp.getRootNode().hasChild(rootNode)) {
            simpleApp.getRootNode().detachChild(rootNode);
        } else {
            simpleApp.getRootNode().attachChild(rootNode);
        }
    }
    
    private void createCheckerBoard() {
        Quad quad = new Quad(60, 40);
        Geometry grid = new Geometry("CheckerBoard", quad);
        grid.rotate(-FastMath.HALF_PI, 0, 0);
        grid.center();

        Material mat = new Material(assetManager, "MatDefs/Tool/CheckerBoard.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.7f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setPolyOffset(1f, 1f);
        grid.setMaterial(mat);
        
        grid.setShadowMode(ShadowMode.Receive);
        grid.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(grid);
    }
    
}
