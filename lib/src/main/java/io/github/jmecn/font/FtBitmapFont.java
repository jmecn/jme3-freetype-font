package io.github.jmecn.font;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.texture.Image;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Page;

import java.io.File;
import java.util.List;

/**
 * desc:
 */
public class FtBitmapFont extends BitmapFont {

    private FtBitmapCharacterSet charSet;
    private boolean flipped;
    private boolean integer;
    private boolean ownsTexture;

    public FtBitmapFont(AssetManager assetManager, File file, int size) {
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Common/MatDefs/Misc/Unshaded.j3md"));
        FtFontGenerator generator = new FtFontGenerator(file);
        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(size);
        parameter.setMatDef(matDef);
        this.charSet = generator.generateData(parameter);
        super.setCharSet(charSet);
    }

    public FtBitmapFont(FtBitmapCharacterSet charSet, boolean integer) {
        this.flipped = charSet.flip;
        this.charSet = charSet;
        this.integer = integer;

        ownsTexture = false;

        // init super
        super.setCharSet(charSet);
    }

    /** @return whether the texture is owned by the font, font disposes the texture itself if true */
    public boolean ownsTexture () {
        return ownsTexture;
    }

    /**
     * Sets whether the font owns the texture. In case it does, the font will also dispose of the texture when {@link ()}
     * is called. Use with care!
     * @param ownsTexture whether the font owns the texture */
    public void setOwnsTexture (boolean ownsTexture) {
        this.ownsTexture = ownsTexture;
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
}
