package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public interface Font {

    String getFullName();
    String getFamilyName();
    String getStyleName();
    String getName();
    float getSize();
    FontFile getFontResource();
    FontStrike getStrike(BaseTransform transform);
    FontStrike getStrike(BaseTransform transform, int smoothingType);
    int getFeatures();
}
