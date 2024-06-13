package io.github.jmecn.font.delegate;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.MaterialDef;
import com.jme3.material.Materials;
import com.jme3.texture.Image;
import io.github.jmecn.font.bmfont.FtBitmapCharacterSet;
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

        BitmapFont font = new BitmapFont();
        font.setCharSet(charSet);

        BitmapText text = new BitmapText(font, false, false);

        ///// important, this line will add a listener to packer, so new BitmapTextPage will be created when needed
        // charSet.registerText(text);

        text.setText("A");
        text.updateLogicalState(0f);// trigger the delegate
        assertEquals(1, text.getChildren().size());

        text.setText("B");
        text.updateLogicalState(0f);// trigger the delegate
        assertEquals(3, text.getChildren().size());

        text.setText("C");
        text.updateLogicalState(0f);// trigger the delegate
        assertEquals(4, text.getChildren().size());

        text.setText("D");
        text.updateLogicalState(0f);// trigger the delegate
        assertEquals(5, text.getChildren().size());

    }

}
