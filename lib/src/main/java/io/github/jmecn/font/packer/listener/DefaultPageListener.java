package io.github.jmecn.font.packer.listener;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class DefaultPageListener implements PageListener {
    static Logger logger = LoggerFactory.getLogger(DefaultPageListener.class);

    private final FtFontParameter parameter;
    private final FtBitmapCharacterSet data;

    public DefaultPageListener(FtFontParameter parameter, FtBitmapCharacterSet data) {
        this.parameter = parameter;
        this.data = data;
    }

    @Override
    public void onPageAdded(Packer packer, PackStrategy strategy, Page page) {
        logger.debug("New page detected: {}", page);
        if (parameter.getMatDef() == null) {
            logger.warn("Material define is null");
            return;
        }

        Image image = page.getImage();
        Texture2D texture2D = new Texture2D(image);
        texture2D.setMinFilter(parameter.getMinFilter());
        texture2D.setMagFilter(parameter.getMagFilter());

        Material material = new Material(parameter.getMatDef());
        material.setTexture("ColorMap", texture2D);
        material.setBoolean("VertexColor", true);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        data.addMaterial(page.getIndex(), material);
    }
}