package io.github.jmecn.font.image;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.TempVars;
import io.github.jmecn.font.utils.ImageUtils;

/**
 * This example proves that Image can be modified in runtime.
 *
 * @author yanmaoyuan
 */
public class TestImageRaster extends SimpleApplication {

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Test image raster");
        settings.setResolution(800, 600);
        settings.setSamples(4);

        TestImageRaster app = new TestImageRaster();
        app.setSettings(settings);
        app.start();
    }

    public static final int SIZE = 32;
    private Image image;
    private ImageRaster raster;

    private Geometry geom;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0.5006529f, 0.5027522f, 1.65945f));
        cam.setRotation(new Quaternion(1.9073439E-6f, 0.9999976f, 0.0019531231f, -9.76561E-4f));

        viewPort.setBackgroundColor(ColorRGBA.LightGray);

        image = ImageUtils.newImage(Image.Format.RGBA8, SIZE, SIZE);
        raster = ImageRaster.create(image);

        Texture2D texture = new Texture2D(image);
        texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        texture.setMagFilter(Texture.MagFilter.Nearest);

        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setTexture("ColorMap", texture);

        Quad quad = new Quad(1, 1);
        geom = new Geometry("quad", quad);
        geom.setMaterial(material);
        rootNode.attachChild(geom);

        inputManager.addMapping("pick", new MouseButtonTrigger(0));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                pick();
            }
        }, "pick");
    }

    private void pick() {
        TempVars vars = TempVars.get();

        Vector3f dir = vars.vect1;
        cam.getWorldCoordinates(inputManager.getCursorPosition(), 1f, dir);
        dir.subtractLocal(cam.getLocation()).normalizeLocal();
        Ray ray = new Ray(cam.getLocation(), dir);

        CollisionResults results = new CollisionResults();
        geom.collideWith(ray, results);
        if (results.size() == 0) {
            vars.release();
            return;
        }
        CollisionResult result = results.getClosestCollision();
        Vector3f point = result.getContactPoint();

        Vector3f store = vars.vect1;
        geom.worldToLocal(point, store);

        int x = (int) (store.x * SIZE);
        int y = (int) (store.y * SIZE);
        raster.setPixel(x, y, ColorRGBA.Red);
        vars.release();
    }

}
