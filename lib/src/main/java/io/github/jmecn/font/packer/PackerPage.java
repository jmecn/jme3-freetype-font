package io.github.jmecn.font.packer;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class PackerPage {
    int index;
    private final Map<String, Rectangle> rectangles;
    private final List<String> names;

    ImageRaster imageRaster;

    Image image;

    boolean dirty;

    public PackerPage(Packer packer) {
        int size = packer.pageWidth * packer.pageHeight * packer.format.getBitsPerPixel();
        ByteBuffer buffer = BufferUtils.createByteBuffer(size);
        image = new Image(packer.format, packer.pageWidth, packer.pageHeight, buffer, ColorSpace.Linear);
        imageRaster = ImageRaster.create(image);

        rectangles = new HashMap<>();
        names = new ArrayList<>();
    }

    public void put(String name, Rectangle rect) {
        rectangles.put(name, rect);
        names.add(name);
    }

    public Rectangle get(String name) {
        return rectangles.get(name);
    }


    /** Creates the texture if it has not been created, else reuploads the entire page pixmap to the texture if the pixmap has
     * changed since this method was last called.
     * @return true if the texture was created or reuploaded. */
    public boolean updateTexture (Texture.MinFilter minFilter, Texture.MagFilter magFilter, boolean useMipMaps) {
        if (image != null) {
            if (!dirty) return false;
            texture.load(texture.getTextureData());
        } else {
            texture = new Texture(new PixmapTextureData(image, image.getFormat(), useMipMaps, false, true)) {
                @Override
                public void dispose () {
                    super.dispose();
                    image.dispose();
                }
            };
            texture.setFilter(minFilter, magFilter);
        }
        dirty = false;
        return true;
    }

    public void drawImage(Image image, int x, int y) {
        // TODO
        drawImage(image, x, y, 0, 0, image.getWidth(), image.getHeight());
    }

    public void drawImage(Image image, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight) {
        // TODO
    }

    public void drawImage(Image image, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth,
                          int dstHeight) {
        // TODO
    }
}
