package io.github.jmecn.font;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;

import java.io.File;

/**
 * desc:
 */
public class FtBitmapFont extends BitmapFont {

    private FtBitmapCharacterSet charSet;

    public FtBitmapFont(AssetManager assetManager, String font) {
        this(assetManager, new AssetKey<>(font), FtFontParameter.DEFAULT_FONT_SIZE);
    }

    public FtBitmapFont(AssetManager assetManager, String font, int size) {
        this(assetManager, new AssetKey<>(font), size);
    }

    public FtBitmapFont(AssetManager assetManager, AssetKey<?> font, int size) {
        AssetInfo info = assetManager.locateAsset(font);
        FtFontGenerator generator = new FtFontGenerator(info.openStream());

        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(size);
        parameter.setMatDef(assetManager.loadAsset(new AssetKey<>(parameter.getMatDefName())));

        setCharSet(generator.generateData(parameter));
    }

    public FtBitmapFont(AssetManager assetManager, File file, int size) {
        FtFontGenerator generator = new FtFontGenerator(file);

        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(size);
        parameter.setMatDef(assetManager.loadAsset(new AssetKey<>(parameter.getMatDefName())));

        setCharSet(generator.generateData(parameter));
    }

    public FtBitmapFont(FtBitmapCharacterSet charSet) {
        setCharSet(charSet);
    }

    public void setCharSet(FtBitmapCharacterSet charSet) {
        super.setCharSet(charSet);
        this.charSet = charSet;
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
    }
}
