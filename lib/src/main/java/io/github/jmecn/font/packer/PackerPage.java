package io.github.jmecn.font.packer;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import io.github.jmecn.font.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static Logger logger = LoggerFactory.getLogger(PackerPage.class);

    int index;
    private final Map<String, Rectangle> rectangles;
    private final List<String> names;

    ImageRaster imageRaster;

    Image image;

    Texture2D texture;

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
        if (texture != null) {
            if (!dirty) {
                return false;
            }
        } else {
            texture = new Texture2D(image);
            texture.setMinFilter(minFilter);
            texture.setMagFilter(magFilter);
        }
        image.setUpdateNeeded();
        dirty = false;
        return true;
    }

    public void drawImage(Image image, int x, int y) {
        logger.info("draw image, {}, pos({},{})", image, x, y);
        ImageUtils.drawImage(this.image, image, x, y);
    }

    public void drawImage(Image image, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight) {
        logger.info("draw image, {}, pos({},{}), src.pos({}, {}) src.size({}, {})", image, x, y, srcx, srcy, srcWidth, srcHeight);
        ImageUtils.drawImage(this.image, image, x, y);
    }

    public void drawImage(Image image, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth,
                          int dstHeight) {
        // TODO
    }
}
