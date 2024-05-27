package io.github.jmecn.font.packer;

import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import io.github.jmecn.font.packer.listener.PageListener;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Packer implements AutoCloseable {

    static Logger logger = LoggerFactory.getLogger(Packer.class);

    int pageWidth;
    int pageHeight;
    int padding;
    boolean duplicateBorder;
    boolean stripWhitespaceX;
    boolean stripWhitespaceY;
    int alphaThreshold;
    private ColorRGBA transparentColor = new ColorRGBA(0, 0, 0, 0);
    private final List<Page> pages;
    private boolean dirty;// use this flag to determine whether the packer is dirty, which means the packer is dirty if any page is dirty. good for performance.
    Image.Format format;
    PackStrategy packStrategy;

    private List<PageListener> listeners;

    public Packer(int pageWidth, int pageHeight, int padding) {
        this(Image.Format.RGBA8, pageWidth, pageHeight, padding, false, new GuillotineStrategy());
    }

    public Packer(Image.Format format, int pageWidth, int pageHeight, int padding, boolean duplicateBorder) {
        this(format, pageWidth, pageHeight, padding, duplicateBorder, new SkylineStrategy());
    }

    public Packer(Image.Format format, int pageWidth, int pageHeight, int padding, boolean duplicateBorder, PackStrategy packStrategy) {
        this.format = format;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.padding = padding;
        this.duplicateBorder = duplicateBorder;
        this.pages = new ArrayList<>();
        this.packStrategy = packStrategy;
        this.listeners = new ArrayList<>();
    }

    private boolean existsListener(PageListener listener) {
        return listeners.contains(listener);
    }

    public void addListener(PageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        if (!existsListener(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(PageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        if (existsListener(listener)) {
            listeners.remove(listener);
        }
    }

    /**
     * Sort the images by area.
     * @param images The images to sort.
     */
    public void sort(List<Rectangle> images) {
        packStrategy.sort(images);
    }


    /**
     * Inserts the pixmap without a name. It cannot be looked up by name.
     * @see #pack(String, Image)
     * @param image The image to pack.
     * @return Rectangle describing the area the image was rendered to.
     */
    public synchronized Rectangle pack(Image image) {
        return pack(null, image);
    }

    /**
     * Inserts the image. If name was not null, you can later retrieve the image's position in the output image via
     * {@link #getRect(String)}.
     * @param name If null, the image cannot be looked up by name.
     * @param image The image to pack.
     * @return Rectangle describing the area the image was rendered to.
     * @throws RuntimeException in case the image did not fit due to the page size being too small or providing a duplicate name.
     */
    public synchronized Rectangle pack(String name, Image image) {
        if (name != null && getRect(name) != null) {
            throw new IllegalArgumentException("Image has already been packed with name: " + name);
        }

        Rectangle rect = new Rectangle(0, 0, image.getWidth(), image.getHeight());

        if (rect.getWidth() > pageWidth || rect.getHeight() > pageHeight) {
            logger.info("Image dose not fit, page size:{}, {}, rect size:{}, {}", pageWidth, pageHeight, rect.getWidth(), rect.getHeight());
            if (name == null)  {
                throw new IllegalArgumentException("Page size too small for page.");
            } else {
                throw new IllegalArgumentException("Page size too small for page: " + name);
            }
        }

        Page page = packStrategy.pack(this, name, rect);
        rect.setPage(page.index);
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
            // min(-1, -1), max(width, height)
            int left = rectX - 1;
            int top = rectY - 1;
            int right = rectX + rectWidth;
            int bottom = rectY + rectHeight;
            // Copy corner pixels to fill corners of the padding.
            ImageUtils.drawImage(page.image, image, 0, 0, 1, 1, left, top);
            ImageUtils.drawImage(page.image, image, imageWidth - 1, 0, 1, 1, right, top);
            ImageUtils.drawImage(page.image, image, 0, imageHeight - 1, 1, 1, left, bottom);
            ImageUtils.drawImage(page.image, image, imageWidth - 1, imageHeight - 1, 1, 1, right, bottom);
            // Copy edge pixels into padding.
            ImageUtils.drawImage(page.image, image, 0, 0, imageWidth, 1, rectX, top);
            ImageUtils.drawImage(page.image, image, 0, imageHeight - 1, imageWidth, 1, rectX, bottom);
            ImageUtils.drawImage(page.image, image, 0, 0, 1, imageHeight, left, rectY);
            ImageUtils.drawImage(page.image, image, imageWidth - 1, 0, 1, imageHeight, right, rectY);
        }

        // mark page as dirty, so the mipmap can be re-generated
        page.setDirty(true);
        setDirty(true);
        return rect;
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    public Page peek() {
        if (pages.isEmpty()) {
            return null;
        }
        return pages.get(pages.size() - 1);
    }

    public void addPage(Page page) {
        page.index = pages.size();
        pages.add(page);

        // notify listeners
        for (PageListener listener : listeners) {
            listener.onPageAdded(this, packStrategy, page);
        }
    }

    /**
     * @return the {@link Page} instances created so far. If multiple threads are accessing the packer,
     * iterating over the pages must be done only after synchronizing on the packer.
     */
    public List<Page> getPages() {
        return pages;
    }

    /**
     * @param name the name of the image
     * @return the rectangle for the image in the page it's stored in or null
     */
    public synchronized Rectangle getRect(String name) {
        for (Page page : pages) {
            Rectangle rect = page.get(name);
            if (rect != null) return rect;
        }
        return null;
    }

    /**
     * @param name the name of the image
     * @return the page the image is stored in or null
     */
    public synchronized Page getPage(String name) {
        for (Page page : pages) {
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
        for (Page page : pages) {
            page.getImage().dispose();
        }
    }

    public boolean isDirty() {
        return dirty;
    }
    public synchronized void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
