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
import com.jme3.texture.Image;
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
public class TestGenerateMaterial extends SimpleApplication {

    static final String FONT = "font/NotoSerifSC-Regular.otf";
    static final String DASHIZHI = "大势至法王子。与其同伦。五十二菩萨。即从座起。顶礼佛足。而白佛言： ‘我忆往昔。恒河沙劫。有佛出世。名无量光。十二如来。相继一劫。其最后佛。名超日月光。 彼佛教我。念佛三昧。譬如有人。一专为忆。一人专忘。如是二人。若逢不逢。或见非见。二人相忆。二忆念深。 如是乃至。从生至生。同于形影。不相乖异。十方如来。怜念众生。如母忆子。若子逃逝。虽忆何为。子若忆母。如母忆时。母子历生。不相违远。若众生心。忆佛念佛。现前当来。必定见佛。去佛不远。不假方便。自得心开。如染香人。身有香气。此则名曰。香光庄严。我本因地。以念佛心。入无生忍。今于此界。摄念佛人。归于净土。佛问圆通。我无选择。都摄六根。净念相继。得三摩地。斯为第一。";
    public static void main(String[] args) throws Exception {
        TestGenerateMaterial app = new TestGenerateMaterial();
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
        Packer packer = new Packer(Image.Format.RGBA8, 1024, 1024, 1, false, new GuillotineStrategy());

        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(packer);
        parameter.setMatDef(matDef);
        parameter.setSize(24);

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
