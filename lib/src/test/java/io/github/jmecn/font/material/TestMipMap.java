package io.github.jmecn.font.material;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.bmfont.CommonChars;
import io.github.jmecn.font.bmfont.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestMipMap extends SimpleApplication {

    static final String FONT = "font/NotoSerifSC-Regular.otf";
    public static void main(String[] args) throws Exception {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setSamples(4);

        TestMipMap app = new TestMipMap();
        app.setSettings( settings );
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(0.76040286f, 1.2665942f, 1.436953f));
        cam.setRotation(new Quaternion(-0.0062463903f, 0.96503586f, 0.02309356f, 0.26102385f));

        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        MaterialDef matDef = (MaterialDef) assetManager.loadAsset(Materials.UNSHADED);

        FtFontGenerator generator = new FtFontGenerator(new File(FONT));

        // use predefined packer
        Packer packer = new Packer(Image.Format.RGBA8, 1024, 1024, 1, false, new GuillotineStrategy());

        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(packer);
        parameter.setMatDef(matDef);
        parameter.setSize(24);
        parameter.setGenMipMaps(true);
        parameter.setMagFilter(Texture.MagFilter.Bilinear);
        parameter.setMinFilter(Texture.MinFilter.Trilinear);

        parameter.setColor(ColorRGBA.White);
        parameter.setPadding(2, 2, 0, 0);

        parameter.setCharacters(CommonChars.SIMPLIFIED_CHINESE.getChars());
        parameter.setIncremental(true);

        FtBitmapCharacterSet data = generator.generateData(parameter);

        //DebugPrintUtils.drawGlyphRect(data);

        int pageSize = data.getPageSize();
        float size = 2f;
        for (int i = 0; i < pageSize; i++) {

            Geometry geom = new Geometry("image#" + i, new Quad(size, size));

            Material mat = data.getMaterial(i);
            geom.setMaterial(mat);

            geom.setLocalTranslation(i * size, 0, 0);
            rootNode.attachChild(geom);
        }
    }
}
