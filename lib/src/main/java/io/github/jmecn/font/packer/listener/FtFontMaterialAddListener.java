package io.github.jmecn.font.packer.listener;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import io.github.jmecn.font.bmfont.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a new page is added, add the image to the character set.
 */
public class FtFontMaterialAddListener implements PageListener {
    static Logger logger = LoggerFactory.getLogger(FtFontMaterialAddListener.class);

    private final FtFontParameter parameter;
    private final FtBitmapCharacterSet data;

    public FtFontMaterialAddListener(FtFontParameter parameter, FtBitmapCharacterSet data) {
        this.parameter = parameter;
        this.data = data;
    }

    @Override
    public void onPageAdded(Packer packer, PackStrategy strategy, Page page) {
        Image image = page.getImage();
        data.addImage(image);

        if (parameter.getMatDef() == null) {
            logger.warn("Material define is null");
            return;
        }

        Texture2D texture2D = new Texture2D(image);
        texture2D.setMinFilter(parameter.getMinFilter());
        texture2D.setMagFilter(parameter.getMagFilter());
        texture2D.setAnisotropicFilter(8);

        Material material = new Material(parameter.getMatDef());
        material.setTexture(parameter.getColorMapParamName(), texture2D);
        if (parameter.isUseVertexColor()) {
            material.setBoolean(parameter.getVertexColorParamName(), true);
        }
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        data.addMaterial(page.getIndex(), material);
    }
}
