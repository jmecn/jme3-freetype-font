package io.github.jmecn.font;

import io.github.jmecn.font.freetype.FtFace;
import io.github.jmecn.font.freetype.FtLibrary;

class FTDisposer implements DisposerRecord  {
    FtLibrary library;
    FtFace face;

    FTDisposer(FtLibrary library, FtFace face) {
        this.library = library;
        this.face = face;
    }

    @Override
    public synchronized void dispose() {
        if (face != null) {
            face.close();
            if (PrismFontFactory.debugFonts) {
                System.err.println("Done Face=" + face);
            }
            face = null;
        }
        if (library != null) {
            library.close();
            if (PrismFontFactory.debugFonts) {
                System.err.println("Done Library=" + library);
            }
            library = null;
        }
    }
}