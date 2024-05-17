package io.github.jmecn.font.editor.ui.window;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 * invokes the update(tpf) method for each window added to the WindowManager.
 */
class WindowUpdaterState extends BaseAppState {

    private final SimpleWindowManager windowManager;

    public WindowUpdaterState(SimpleWindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override protected void initialize(Application app) { }
    @Override protected void cleanup(Application app) { }
    @Override protected void onEnable() { }
    @Override protected void onDisable() { }

    @Override
    public void update(float tpf) {
        windowManager.getWindowList().executeWindowUpdateLoops(tpf);
    }

}
