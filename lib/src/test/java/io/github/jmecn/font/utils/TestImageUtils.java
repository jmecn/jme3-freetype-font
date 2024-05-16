package io.github.jmecn.font.utils;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class TestImageUtils extends SimpleApplication {
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        TestImageUtils app = new TestImageUtils();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        int width = cam.getWidth();
        int height = cam.getHeight();
        Image image = ImageUtils.newImage(Image.Format.RGBA8, width, height);

        // random lines
        for (int i = 0; i < 100; i++) {
            ImageUtils.drawLine(image,
                    (int) (Math.random() * width),
                    (int) (Math.random() * height),
                    (int) (Math.random() * width),
                    (int) (Math.random() * height),
                    ColorRGBA.randomColor());
        }

        Texture2D texture = new Texture2D(image);
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

        // display the image
        Picture picture = new Picture("image");
        picture.setTexture(assetManager, texture, true);
        picture.setWidth(width);
        picture.setHeight(height);
        guiNode.attachChild(picture);
    }
}
