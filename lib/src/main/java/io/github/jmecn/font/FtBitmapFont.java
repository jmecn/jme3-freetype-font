package io.github.jmecn.font;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.GlyphRun;

import java.io.File;

/**
 * desc:
 */
public class FtBitmapFont extends BitmapFont {

    private final FtBitmapCharacterSet charSet;

    public FtBitmapFont(AssetManager assetManager, File file, int size) {
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Common/MatDefs/Misc/Unshaded.j3md"));
        FtFontGenerator generator = new FtFontGenerator(file);
        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(size);
        parameter.setMatDef(matDef);
        this.charSet = generator.generateData(parameter);
        super.setCharSet(charSet);
    }

    public FtBitmapFont(FtBitmapCharacterSet charSet) {
        this.charSet = charSet;
        // init super
        super.setCharSet(charSet);
    }

    @Override
    public FtBitmapCharacterSet getCharSet() {
        return charSet;
    }

    @Override
    public Material getPage(int page) {
        return charSet.getMaterial(page);
    }

    @Override
    public int getPageSize() {
        return charSet.getPageSize();
    }

    public void payload(String text) {
        charSet.getGlyphs(new GlyphRun(), text, 0, text.length(), null);
    }
}
