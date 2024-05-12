package io.github.jmecn.font.packer;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import io.github.jmecn.font.packer.strategy.BiTreePackStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Packer implements AutoCloseable {
    int pageWidth;
    int pageHeight;
    int padding;
    boolean duplicateBorder;
    boolean stripWhitespaceX;
    boolean stripWhitespaceY;
    int alphaThreshold;
    private ColorRGBA transparentColor = new ColorRGBA(0, 0, 0, 0);
    private final LinkedList<PackerPage> pages;
    Image.Format format;
    PackStrategy packStrategy;

    public Packer(Image.Format format, int pageWidth, int pageHeight, int padding, boolean duplicateBorder) {
        // use ScanlinePackStrategy by default
        this(format, pageWidth, pageHeight, padding, duplicateBorder, new BiTreePackStrategy());
    }

    public Packer(Image.Format format, int pageWidth, int pageHeight, int padding, boolean duplicateBorder, PackStrategy packStrategy) {
        this.format = format;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.padding = padding;
        this.duplicateBorder = duplicateBorder;
        this.pages = new LinkedList<>();
        this.packStrategy = packStrategy;
    }

    public void sort(List<Rectangle> images) {
        packStrategy.sort(images);
    }


    /** Inserts the pixmap without a name. It cannot be looked up by name.
     * @see #pack(String, Image) */
    public synchronized Rectangle pack(Image image) {
        return pack(null, image);
    }

    /**
     * Inserts the image. If name was not null, you can later retrieve the image's position in the output image via
     * {@link #getRect(String)}.
     * @param name If null, the image cannot be looked up by name.
     * @return Rectangle describing the area the image was rendered to.
     * @throws RuntimeException in case the image did not fit due to the page size being too small or providing a duplicate name.
     */
    public synchronized Rectangle pack(String name, Image image) {
        if (name != null && getRect(name) != null) {
            throw new IllegalArgumentException("Image has already been packed with name: " + name);
        }

        Rectangle rect = new Rectangle(0, 0, image.getWidth(), image.getHeight());

        if (rect.getWidth() > pageWidth || rect.getHeight() > pageHeight) {
            if (name == null)  {
                throw new IllegalArgumentException("PackerPage size too small for page.");
            } else {
                throw new IllegalArgumentException("PackerPage size too small for page: " + name);
            }
        }

        PackerPage page = packStrategy.pack(this, name, rect);
        if (name != null) {
            page.put(name, rect);
        }

        int rectX = rect.x;
        int rectY = rect.y;
        int rectWidth = rect.width;
        int rectHeight = rect.height;

        page.drawImage(image, rectX, rectY);

        if (duplicateBorder) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            // Copy corner pixels to fill corners of the padding.
            page.drawImage(image, 0, 0, 1, 1, rectX - 1, rectY - 1, 1, 1);
            page.drawImage(image, imageWidth - 1, 0, 1, 1, rectX + rectWidth, rectY - 1, 1, 1);
            page.drawImage(image, 0, imageHeight - 1, 1, 1, rectX - 1, rectY + rectHeight, 1, 1);
            page.drawImage(image, imageWidth - 1, imageHeight - 1, 1, 1, rectX + rectWidth, rectY + rectHeight, 1, 1);
            // Copy edge pixels into padding.
            page.drawImage(image, 0, 0, imageWidth, 1, rectX, rectY - 1, rectWidth, 1);
            page.drawImage(image, 0, imageHeight - 1, imageWidth, 1, rectX, rectY + rectHeight, rectWidth, 1);
            page.drawImage(image, 0, 0, 1, imageHeight, rectX - 1, rectY, 1, rectHeight);
            page.drawImage(image, imageWidth - 1, 0, 1, imageHeight, rectX + rectWidth, rectY, 1, rectHeight);
        }

        return rect;
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    public PackerPage peek() {
        return pages.peekLast();
    }

    public void addPage(PackerPage page) {
        page.index = pages.size();
        pages.add(page);
    }

    /**
     * @return the {@link PackerPage} instances created so far. If multiple threads are accessing the packer,
     * iterating over the pages must be done only after synchronizing on the packer.
     */
    public List<PackerPage> getPages() {
        return pages;
    }

    /**
     * @param name the name of the image
     * @return the rectangle for the image in the page it's stored in or null
     */
    public synchronized Rectangle getRect(String name) {
        for (PackerPage page : pages) {
            Rectangle rect = page.get(name);
            if (rect != null) return rect;
        }
        return null;
    }

    /**
     * @param name the name of the image
     * @return the page the image is stored in or null
     */
    public synchronized PackerPage getPage(String name) {
        for (PackerPage page : pages) {
            Rectangle rect = page.get(name);
            if (rect != null) return page;
        }
        return null;
    }

    /** Returns the index of the page containing the given packed rectangle.
     * @param name the name of the image
     * @return the index of the page the image is stored in or -1 */
    public synchronized int getPageIndex(String name) {
        for (int i = 0; i < pages.size(); i++) {
            Rectangle rect = pages.get(i).get(name);
            if (rect != null) return i;
        }
        return -1;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getPadding() {
        return padding;
    }

    public boolean isDuplicateBorder() {
        return duplicateBorder;
    }

    public boolean isStripWhitespaceX() {
        return stripWhitespaceX;
    }

    public boolean isStripWhitespaceY() {
        return stripWhitespaceY;
    }

    public int getAlphaThreshold() {
        return alphaThreshold;
    }

    public ColorRGBA getTransparentColor() {
        return transparentColor;
    }

    public void setTransparentColor(ColorRGBA color) {
        this.transparentColor.set(color);
    }

    public Image.Format getFormat() {
        return format;
    }

    public PackStrategy getPackStrategy() {
        return packStrategy;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
