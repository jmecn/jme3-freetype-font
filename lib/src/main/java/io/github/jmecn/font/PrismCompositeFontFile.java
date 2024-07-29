package io.github.jmecn.font;

import io.github.jmecn.math.BaseTransform;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/*
 * Wraps a physical font and adds an appropriate fallback resource.
 */

class PrismCompositeFontFile implements CompositeFontFile {

    private FontFile primaryResource;
    private FallbackFile fallbackResource; // is a composite too.

    PrismCompositeFontFile(FontFile primaryResource,
                           String lookupName) {
        // remind go through and make the typing better.
        if (!(primaryResource instanceof FontFileImpl)) {
            Thread.dumpStack();
            throw new IllegalStateException("wrong resource type");
        }
        if (lookupName != null) {
            PrismFontFactory factory = PrismFontFactory.getFontFactory();
            factory.compResourceMap.put(lookupName, this);
        }
        this.primaryResource = primaryResource;
        fallbackResource = FallbackFile.getFallbackResource(primaryResource);
    }

    @Override
    public int getNumSlots() {
        return fallbackResource.getNumSlots()+1;
    }

    @Override
    public int getSlotForFont(String fontName) {
        if (primaryResource.getFullName().equalsIgnoreCase(fontName)) {
            return 0;
        }
        return fallbackResource.getSlotForFont(fontName) + 1;
    }

    @Override
    public FontFile getSlotResource(int slot) {
        if (slot == 0) {
            return primaryResource;
        } else {
            FontFile fb = fallbackResource.getSlotResource(slot-1);
            if (fb != null) {
                return fb;
            } else {
                 return primaryResource;
            }
        }
    }

    @Override
    public String getFullName() {
        return primaryResource.getFullName();
    }

    @Override
    public String getPostscriptName() {
        return primaryResource.getPostscriptName();
    }

    @Override
    public String getFamilyName() {
        return primaryResource.getFamilyName();
    }

    @Override
    public String getStyleName() {
        return primaryResource.getStyleName();
    }

    @Override
    public String getLocaleFullName() {
        return primaryResource.getLocaleFullName();
    }

    @Override
    public String getLocaleFamilyName() {
        return primaryResource.getLocaleFamilyName();
    }

    @Override
    public String getLocaleStyleName() {
        return primaryResource.getLocaleStyleName();
    }

    @Override
    public String getFileName() {
        return primaryResource.getFileName();
    }

    @Override
    public int getFeatures() {
        return primaryResource.getFeatures();
    }

    @Override
    public boolean isEmbeddedFont() {
        return primaryResource.isEmbeddedFont();
    }

    @Override
    public boolean isBold() {
        return primaryResource.isBold();
    }

    @Override
    public boolean isItalic() {
        return primaryResource.isItalic();
    }

    CompositeGlyphMapper mapper;
    @Override
    public CharToGlyphMapper getGlyphMapper() {
        if (mapper == null) {
            mapper = new CompositeGlyphMapper(this);
        }
        return mapper;
    }

    @Override
    public float[] getGlyphBoundingBox(int glyphCode,
                                float size, float[] retArr) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        FontFile slotResource = getSlotResource(slot);
        return slotResource.getGlyphBoundingBox(slotglyphCode, size, retArr);
    }

    @Override
    public float getAdvance(int glyphCode, float size) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        FontFile slotResource = getSlotResource(slot);
        return slotResource.getAdvance(slotglyphCode, size);
    }

    Map<FontStrikeDesc, WeakReference<FontStrike>> strikeMap = new ConcurrentHashMap<>();

    @Override
    public Map<FontStrikeDesc, WeakReference<FontStrike>> getStrikeMap() {
        return strikeMap;
    }

    @Override
    public int getDefaultAAMode() {
        return getSlotResource(0).getDefaultAAMode();
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform) {
        return getStrike(size, transform, getDefaultAAMode());
    }

    @Override
    public FontStrike getStrike(float size, BaseTransform transform,
                                int aaMode) {
        FontStrikeDesc desc = new FontStrikeDesc(size, transform, aaMode);
        WeakReference<FontStrike> ref = strikeMap.get(desc);
        CompositeStrike strike = null;
        if (ref != null) {
            strike = (CompositeStrike)ref.get();
        }
        if (strike == null) {
            strike = new CompositeStrike(this, size, transform, aaMode, desc);
            if (strike.disposer != null) {
                ref = Disposer.addRecord(strike, strike.disposer);
            } else {
                ref = new WeakReference<>(strike);
            }
            strikeMap.put(desc, ref);
        }
        return strike;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PrismCompositeFontFile)) {
            return false;
        }
        final PrismCompositeFontFile other = (PrismCompositeFontFile)obj;
        return primaryResource.equals(other.primaryResource);
    }

    @Override
    public int hashCode() {
        return primaryResource.hashCode();
    }
}