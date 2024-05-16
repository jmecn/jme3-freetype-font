package io.github.jmecn.font.delegate;

import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Spatial;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.exception.FtRuntimeException;
import net.bytebuddy.implementation.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class is a delegate to BitmapText, it overrides the private method assemble() to support dynamic generated BitmapTextPage.
 *
 * @author yanmaoyuan
 */
public class BitmapTextDelegate {
    static Logger logger = LoggerFactory.getLogger(BitmapTextDelegate.class);

    static Field textPagesField;
    static Field needRefreshField;
    static Field lettersField;
    static Field fontField;

    // Letters#update()
    static Method lettersUpdateMethod;

    // BitmapTextPage#assemble(Letters)
    static Method bitmapTextPageAssembleMethod;

    static Class<?> clazzBitmapTextPage;
    static Constructor<?> constructor;

    static {
        try {
            textPagesField = BitmapText.class.getDeclaredField("textPages");
            textPagesField.setAccessible(true);
            needRefreshField = BitmapText.class.getDeclaredField("needRefresh");
            needRefreshField.setAccessible(true);
            lettersField = BitmapText.class.getDeclaredField("letters");
            lettersField.setAccessible(true);
            fontField = BitmapText.class.getDeclaredField("font");
            fontField.setAccessible(true);

            Class<?> letters = Class.forName("com.jme3.font.Letters");
            lettersUpdateMethod = letters.getDeclaredMethod("update");
            lettersUpdateMethod.setAccessible(true);

            clazzBitmapTextPage = Class.forName("com.jme3.font.BitmapTextPage");
            constructor = clazzBitmapTextPage.getDeclaredConstructor(BitmapFont.class, boolean.class, int.class);
            constructor.setAccessible(true);// allow access to package-private constructor

            bitmapTextPageAssembleMethod = clazzBitmapTextPage.getDeclaredMethod("assemble", letters);
            bitmapTextPageAssembleMethod.setAccessible(true);
        } catch (Exception e) {
            throw new FtRuntimeException("Failed to init BitmapTextDelegate", e);
        }
    }

    public static void assemble(@This Object obj) throws Throwable {
        BitmapText text = (BitmapText) obj;
        BitmapCharacterSet charSet = text.getFont().getCharSet();

        Object letters = lettersField.get(obj);
        Object[] textPages = (Object[]) textPagesField.get(obj);

        if (charSet instanceof FtBitmapCharacterSet) {
            FtBitmapCharacterSet ftCharSet = (FtBitmapCharacterSet) charSet;
            int pageSize = ftCharSet.getPageSize();
            logger.debug("page size:{}, current:{}", pageSize, textPages.length);
            if (pageSize > textPages.length) {
                Object array = Array.newInstance(clazzBitmapTextPage, pageSize);
                for (int i = 0; i < textPages.length; i++) {
                    Array.set(array, i, textPages[i]);
                }
                for (int index = textPages.length; index < pageSize; index++) {
                    Object newPage = constructor.newInstance(text.getFont(), true, index);
                    Array.set(array, index, newPage);
                    text.attachChild((Spatial) newPage);
                    logger.debug("create new page: {}", index);
                }
                textPagesField.set(obj, array);
                textPages = (Object[]) array;
            }
        }

        // first generate quad list
        lettersUpdateMethod.invoke(letters);
        for (int i = 0; i < textPages.length; i++) {
            bitmapTextPageAssembleMethod.invoke(textPages[i], letters);
        }
        needRefreshField.set(obj, false);
    }
}
