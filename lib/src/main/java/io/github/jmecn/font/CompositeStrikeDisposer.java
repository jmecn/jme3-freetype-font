package io.github.jmecn.font;

import java.lang.ref.WeakReference;

class CompositeStrikeDisposer implements DisposerRecord {

    FontFile fontFile;
    FontStrikeDesc desc;
    boolean disposed = false;

    public CompositeStrikeDisposer(FontFile font, FontStrikeDesc desc) {
        this.fontFile = font;
        this.desc = desc;
    }

    @Override
    public synchronized void dispose() {
        if (!disposed) {
            // Careful here. The original strike we are collecting
            // may now be superseded in the map, so only remove
            // the desc if the value reference has been cleared
            WeakReference ref = fontFile.getStrikeMap().get(desc);
            if (ref != null) {
                Object o = ref.get();
                if (o == null) {
                    fontFile.getStrikeMap().remove(desc);
                }
            }

            disposed = true;
        }
    }
}
