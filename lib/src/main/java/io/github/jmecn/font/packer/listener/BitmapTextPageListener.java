package io.github.jmecn.font.packer.listener;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Spatial;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * When the page is added, a new BitmapTextPage will be created, and add it to BitmapText.
 *
 * @author yanmaoyuan
 */
public class BitmapTextPageListener implements PageListener {

    static Logger logger = LoggerFactory.getLogger(BitmapTextPageListener.class);

    private final BitmapText text;
    private final Field textPagesField;
    private final Class<?> clazzBitmapTextPage;
    private final Constructor<?> constructor;


    public BitmapTextPageListener(BitmapText text) {
        this.text = text;
        logger.info("new text registered to font:{}", text.getFont());
        try {
            clazzBitmapTextPage = Class.forName("com.jme3.font.BitmapTextPage");
            constructor = clazzBitmapTextPage.getDeclaredConstructor(BitmapFont.class, boolean.class, int.class);
            constructor.setAccessible(true);// allow access to package-private constructor

            textPagesField = BitmapText.class.getDeclaredField("textPages");
            textPagesField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            logger.error("error", e);
            throw new FtRuntimeException("Failed to access textPages int BitmapText instance", e);
        }
    }

    @Override
    public void onPageAdded(Packer packer, PackStrategy strategy, Page page) {
        int index = page.getIndex();
        logger.debug("An new page is added to BitmapText:{}", index);
        try {
            if (textPagesField.get(text) != null) {
                Object[] old = (Object[]) textPagesField.get(text);
                Object array = Array.newInstance(clazzBitmapTextPage, packer.getPages().size());
                for (int i = 0; i < old.length; i++) {
                    Array.set(array, i, old[i]);
                }
                Object newPage = constructor.newInstance(text.getFont(), true, index);
                Array.set(array, index, newPage);

                textPagesField.set(text, array);
                text.attachChild((Spatial) newPage);
                logger.info("page {} is added", index);
            }
        } catch (ReflectiveOperationException e) {
            logger.error("error", e);
        }
    }
}
