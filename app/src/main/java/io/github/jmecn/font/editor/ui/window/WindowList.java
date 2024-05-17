package io.github.jmecn.font.editor.ui.window;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizes the z-order of windows automatically.
 * Each window added is assigned a z-order.
 */
public class WindowList {

    private final int startZOrder;
    private final List<Window> list = new ArrayList<>();

    /**
     * Creates a z-organized window list.
     */
    public WindowList(int startZOrder) {
        this.startZOrder = startZOrder;
    }

    public int getWindowCount() {
        return list.size();
    }

    public void add(Window window) {

        list.add(window);
        reorganize();
    }

    public boolean remove(Window window) {
        if (list.remove(window)) {
            reorganize();
            return true;
        }

        return false;
    }

    Window getByTitle(String title) {

        return list.stream()
                .filter(element -> element.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);

    }

    Window getById(String id) {

        return list.stream()
                .filter(element -> {

                    String elemId = element.getWindowPanel().getUserData(JmeWindow.WINDOW_ID);

                    if (elemId != null) {
                        return elemId.compareTo(id) == 0;
                    }

                    return false;
                })
                .findFirst()
                .orElse(null);

    }

    public void bringToFront(Window window) {

        if (list.contains(window)) {

            list.remove(window);
            list.add(window);

            reorganize();
        }
    }

    public void sendToBack(Window window) {

        if (list.contains(window)) {

            list.remove(window);
            list.add(0, window);

            reorganize();
        }
    }

    final float margin = .1f;

    private void reorganize() {

        for (int i = 0; i < list.size(); i++) {
            Window element = list.get(i);

            float z;

            if (i == 0) {
                z = startZOrder;
            }
            else {

                Window previous = list.get(i - 1);

                // this works
                z = previous.getWindowPanel().getLocalTranslation().z;
                z += previous.getWindowPanel().getPreferredSize().z;

                // this doesn't
                // BoundingBox bb = (BoundingBox)previous.getWindowContainer().getWorldBound();
                // float zExtent = bb.getCenter().z + bb.getZExtent();
                // z = zExtent;

                z+= margin;
            }

            element.getWindowPanel().setLocalTranslation(
                    element.getWindowPanel().getLocalTranslation().x,
                    element.getWindowPanel().getLocalTranslation().y,
                    z);

        }
    }

    void executeWindowUpdateLoops(float tpf) {
        list.forEach(window -> window.update(tpf));
    }

}
