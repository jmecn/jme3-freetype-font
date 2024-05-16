package io.github.jmecn.font.aop;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.MaterialDef;
import com.jme3.material.Materials;
import com.jme3.texture.Image;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
class TestBitmapText {
    @Test void testLoadClass() {
        // init AssetManager
        AssetManager assetManager = new DesktopAssetManager(true);
        MaterialDef matDef = (MaterialDef) assetManager.loadAsset(Materials.UNSHADED);

        // use a small page size, to test the page listener
        Packer packer = new Packer(Image.Format.RGBA8, 32, 32, 0, false);
        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(packer);
        parameter.setMatDef(matDef);
        parameter.setSize(32);// a small size
        parameter.setIncremental(true);
        parameter.setCharacters("A");

        FtFontGenerator generator = new FtFontGenerator(new File("../font/FreeSerif.ttf"));
        FtBitmapCharacterSet charSet = generator.generateData(parameter);

        BitmapFont font = generator.generateFont(charSet);
        ///// important, this line will add a listener to packer
        BitmapText text = new BitmapText(font, false, false);
        charSet.registerText(text);

        BitmapText[] texts = new BitmapText[10];
        for (int i = 0; i < 10; i++) {
            texts[i] = new BitmapText(font, false, false);
            charSet.registerText(texts[i]);
        }

        text.setText("A");
        assertEquals(1, text.getChildren().size());
        text.setText("B");
        assertEquals(2, text.getChildren().size());
        text.setText("C");
        assertEquals(3, text.getChildren().size());
        text.setText("D");
        assertEquals(4, texts[4].getChildren().size());
    }
}
