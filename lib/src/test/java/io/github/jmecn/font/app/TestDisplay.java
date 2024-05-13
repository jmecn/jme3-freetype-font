package io.github.jmecn.font.app;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestDisplay extends SimpleApplication {

    private final Image[] images;
    private final String material;
    public TestDisplay(String material, Image ... images) {
        super();
        this.images = images;
        if (material == null) {
            this.material = Materials.UNSHADED;
        } else {
            this.material = material;
        }
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        cam.setLocation(new Vector3f(7.2502255f, 8.049806f, 22.52177f));
        cam.setRotation(new Quaternion(-1.7922641E-4f, 0.9989392f, -0.04588203f, -0.00390219f));

        if (flyCam != null) {
            flyCam.setMoveSpeed(10f);
        }

        float size = 2f;
        for (int i = 0; i < images.length; i++) {
            Texture2D texture2D = new Texture2D();
            texture2D.setImage(images[i]);
            texture2D.setMagFilter(Texture.MagFilter.Bilinear);
            texture2D.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

            Geometry geom = new Geometry("image#" + i, new Quad(size, size, true));
            Material material = new Material(assetManager, this.material);
            material.setTexture("ColorMap", texture2D);
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            geom.setMaterial(material);

            geom.setLocalTranslation(i * size, 0, 0);
            rootNode.attachChild(geom);
        }
    }

    public static void run(String material, Image ... images) {
        AppSettings settings = new AppSettings(true);
        settings.setSamples(4);
        settings.setGammaCorrection(false);

        TestDisplay app = new TestDisplay(material, images);
        app.setSettings(settings);
        app.start();
    }
}
