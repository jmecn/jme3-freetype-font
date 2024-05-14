package io.github.jmecn.font.app;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.utils.DebugPrintUtils;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestGenerateMaterialSerif extends SimpleApplication {

    static final String FONT = "font/FreeSerif.ttf";
    public static void main(String[] args) throws Exception {
        TestGenerateMaterialSerif app = new TestGenerateMaterialSerif();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(1.0828096f, 1.250304f, 2.6403296f));
        cam.setRotation(new Quaternion(2.4006075E-4f, 0.9996221f, 0.009274134f, -0.02587497f));

        viewPort.setBackgroundColor(ColorRGBA.LightGray);

        MaterialDef matDef = (MaterialDef) assetManager.loadAsset(Materials.UNSHADED);

        FtFontGenerator generator = new FtFontGenerator(new File(FONT));

        // use predefined packer
        Packer packer = new Packer(Image.Format.RGBA8, 512, 512, 1, false, new SkylineStrategy());

        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(packer);
        parameter.setMatDef(matDef);
        parameter.setSize(32);
        parameter.setColor(ColorRGBA.White);
        parameter.setPadding(1);
        parameter.setCharacters("ABCDEFG");

        FtBitmapCharacterSet data = generator.generateData(parameter);

        DebugPrintUtils.drawGlyphRect(data);

        float size = 2f;
        int pageSize = data.getPageSize();
        for (int i = 0; i < pageSize; i++) {

            Geometry geom = new Geometry("image#" + i, new Quad(size, size, true));

            Material mat = data.getMaterial(i);
            geom.setMaterial(mat);

            geom.setLocalTranslation(i * size, 0, 0);
            rootNode.attachChild(geom);
        }
    }
}
