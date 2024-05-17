package io.github.jmecn.font.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.material.MaterialDef;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.FtBitmapFont;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtFontLoader implements AssetLoader {

    @Override
    public Object load(AssetInfo assetInfo) {
        AssetKey<?> key = assetInfo.getKey();
        AssetManager assetManager = assetInfo.getManager();

        FtFontGenerator generator = new FtFontGenerator(assetInfo.openStream());
        FtFontParameter parameter = newParameter(assetManager, key);
        FtBitmapCharacterSet charSet = generator.generateData(parameter);
        return new FtBitmapFont(charSet);
    }

    private FtFontParameter newParameter(AssetManager assetManager, AssetKey<?> key) {
        FtFontParameter parameter;
        if (key instanceof FtFontKey) {
            FtFontKey ftFontKey = (FtFontKey) key;
            parameter = ftFontKey.getDelegate();
        } else {
            parameter = new FtFontParameter();
        }

        if (parameter.getMatDef() != null) {// check if user defined their own material
            return parameter;
        }

        if (parameter.getMatDefName() == null) {
            throw new IllegalArgumentException("Material is not defined, please set it with setMatDefName(String).");
        }

        // load .j3md
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>(parameter.getMatDefName()));
        parameter.setMatDef(matDef);
        return parameter;
    }
}
