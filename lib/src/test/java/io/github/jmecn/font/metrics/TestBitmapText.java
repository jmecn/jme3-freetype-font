package io.github.jmecn.font.metrics;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.FtBitmapFont;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.utils.DebugPrintUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestBitmapText extends SimpleApplication {

    static Logger logger = LoggerFactory.getLogger(TestBitmapText.class);

    public static void main(String[] args) {
        TestBitmapText app = new TestBitmapText();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        String text = "Gg";

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Common/MatDefs/Misc/Unshaded.j3md"));
        FtFontGenerator generator = new FtFontGenerator(new File("font/Noto_Serif_SC/NotoSerifSC-Regular.otf"));


        Packer packer = new Packer(Image.Format.RGBA8, 64, 32, 0, false);
        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(packer);
        parameter.setSize(32);
        parameter.setMatDef(matDef);
        parameter.setIncremental(true);
        parameter.setMagFilter(Texture.MagFilter.Nearest);
        parameter.setCharacters(text);

        FtBitmapFont fnt = generator.generateFont(parameter);

        DebugPrintUtils.drawGlyphRect(fnt.getCharSet());

        BitmapText txt = new BitmapText(fnt, false);
        txt.setBox(new Rectangle(0, 0, 6, 3));
        txt.setQueueBucket(RenderQueue.Bucket.Transparent);
        txt.setSize( 0.5f );
        txt.setText(text);
        rootNode.attachChild(txt);

        txt.updateLogicalState(0f);

        Spatial spatial = txt.getChild(0);
        logger.info("letter:{}", spatial);

    }
}
