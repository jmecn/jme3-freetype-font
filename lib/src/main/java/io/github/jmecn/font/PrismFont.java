package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;

class PrismFont implements Font {

    private String name;
    private float fontSize;
    protected FontResource fontResource;
    private int features;

    PrismFont(FontResource fontResource, String name, float size) {
        this.fontResource = fontResource;
        this.name = name;
        this.fontSize = size;
    }

    @Override
    public String getFullName() {
        return fontResource.getFullName();
    }

    @Override
    public String getFamilyName() {
        return fontResource.getFamilyName();
    }

    @Override
    public String getStyleName() {
        return fontResource.getStyleName();
    }

    /*
     * Returns the features the user has requested.
     * (kerning, ligatures, etc)
     */
    @Override public int getFeatures() {
        return features;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getSize() {
        return fontSize;
    }

    @Override
    public FontStrike getStrike(BaseTransform transform) {
        return fontResource.getStrike(fontSize, transform);
    }

    @Override
    public FontStrike getStrike(BaseTransform transform,
                                int smoothingType) {
        return fontResource.getStrike(fontSize, transform, smoothingType);
    }

    @Override
    public FontResource getFontResource() {
        return fontResource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PrismFont)) {
            return false;
        }
        final PrismFont other = (PrismFont) obj;

        // REMIND: When fonts can be rendered other than as greyscale
        // and generally differ in ways other than the point size
        // we need to update this method.
        return
            this.fontSize == other.fontSize &&
            this.fontResource.equals(other.fontResource);
    }

    private int hash;
    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        else {
            hash = 497 + Float.floatToIntBits(fontSize);
            hash = 71 * hash + fontResource.hashCode();
            return hash;
        }
    }
}