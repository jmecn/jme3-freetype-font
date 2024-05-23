package io.github.jmecn.font.delegate;

import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.exception.FtRuntimeException;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Field;

/**
 * Override BitmapFont's private method,
 *
 * @author yanmaoyuan
 */
public class BitmapFontDelegate {
    static Field pagesField;

    static {
        try {
            pagesField = BitmapFont.class.getDeclaredField("pages");
            pagesField.setAccessible(true);
        } catch (Exception e) {
            throw new FtRuntimeException("Failed to init BitmapTextDelegate", e);
        }
    }

    /**
     * This is delegate method for BitmapFont::getPage(int index)
     *
     * @param obj the BitmapFont instance
     * @param args page index
     * @return the material of this page
     */
    public static Material getPage(@This Object obj, @AllArguments Object ... args) {
        BitmapFont font = (BitmapFont) obj;
        int index = (int) args[0];
        if (font.getCharSet() instanceof FtBitmapCharacterSet) {
            return ((FtBitmapCharacterSet) font.getCharSet()).getMaterial(index);
        } else {
            Material[] pages;
            try {
                pages = (Material[]) pagesField.get(obj);
                return pages[index];
            } catch (IllegalAccessException e) {
                throw new FtRuntimeException("Failed access [pages] field", e);
            }
        }
    }

    /**
     * This is delegate method for BitmapFont::getPageSize()
     * @param obj the BitmapFont instance
     * @return the pageSize
     */
    public static int getPageSize(@This Object obj) {
        BitmapFont font = (BitmapFont) obj;
        if (font.getCharSet() instanceof FtBitmapCharacterSet) {
            return ((FtBitmapCharacterSet) font.getCharSet()).getPageSize();
        } else {
            Material[] pages;
            try {
                pages = (Material[]) pagesField.get(obj);
                return pages.length;
            } catch (IllegalAccessException e) {
                throw new FtRuntimeException("Failed access [pages] field", e);
            }
        }
    }
}
