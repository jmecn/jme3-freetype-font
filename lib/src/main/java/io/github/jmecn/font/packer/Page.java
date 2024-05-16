package io.github.jmecn.font.packer;

import com.jme3.texture.Image;
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
public class Page {
    static Logger logger = LoggerFactory.getLogger(Page.class);

    int index;
    private final Map<String, Rectangle> rectangles;
    private final List<String> names;

    ImageRaster imageRaster;

    Image image;

    public Page(Packer packer) {
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

    public void drawImage(Image image, int x, int y) {
        ImageUtils.drawImage(this.image, image, x, y, true);
    }

    public int getIndex() {
        return index;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Page{" +
                "index=" + index +
                ", image=" + image +
                '}';
            }
}
