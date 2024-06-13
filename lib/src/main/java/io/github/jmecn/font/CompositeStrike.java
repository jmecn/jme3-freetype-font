package io.github.jmecn.font;

import com.jme3.math.Vector2f;
import io.github.jmecn.math.BaseTransform;
import io.github.jmecn.text.GlyphList;

import java.awt.*;
import java.awt.geom.Path2D;

public class CompositeStrike implements FontStrike {

    private CompositeFontResource fontResource;
    private float size;
    private int aaMode;
    BaseTransform transform;
    private FontStrike slot0Strike;
    private FontStrike[] strikeSlots;

    private FontStrikeDesc desc;
    DisposerRecord disposer;

    @Override
    public void clearDesc() {
        fontResource.getStrikeMap().remove(desc);
        // For a composite strike, you also need to remove the strike
        // refs of the raw fonts. At the least this needs to remove
        // the slot 0 strike, but it may be that the fallback strikes
        // should be left alone as they could be shared. This needs
        // to be re-visited.
        if (slot0Strike != null) {
            slot0Strike.clearDesc();
        }
        if (strikeSlots != null) {
            for (int i=1; i<strikeSlots.length; i++) {
                if (strikeSlots[i] != null) {
                    strikeSlots[i].clearDesc();
                }
            }
        }
    }

    CompositeStrike(CompositeFontResource fontResource,
                    float size, BaseTransform graphicsTransform, int aaMode,
                    FontStrikeDesc desc) {

        this.fontResource = fontResource;
        this.size = size;
        if (graphicsTransform.isTranslateOrIdentity()) {
            this.transform = BaseTransform.IDENTITY_TRANSFORM;
        } else {
            this.transform = graphicsTransform.copy();
        }
        this.desc = desc;
        this.aaMode = aaMode;
        // CompositeStrikes do not directly hold any native resources
        // but we need to free the desc key from the strike map after
        // we find the strike has been GC'd.
        disposer = new CompositeStrikeDisposer(fontResource, desc);
    }

    @Override
    public int getAAMode() {
        PrismFontFactory factory = PrismFontFactory.getFontFactory();
        if (factory.isLCDTextSupported()) {
            return this.aaMode;
        } else {
            return FontResource.AA_GREYSCALE;
        }
    }

    /**
     * Trusting caller to NOT mutate the returned result, to
     * avoid a clone.
     */
    @Override
    public BaseTransform getTransform() {
        return transform;
    }

    public FontStrike getStrikeSlot(int slot) {
        if (slot == 0) {
            if (slot0Strike == null) {
                FontResource slot0Resource = fontResource.getSlotResource(0);
                slot0Strike = slot0Resource.getStrike(size, transform,
                                                      getAAMode());
            }
            return slot0Strike;
        } else {
            if (strikeSlots == null) {
                strikeSlots = new FontStrike[fontResource.getNumSlots()];
            }

            if (slot >= strikeSlots.length) {
                FontStrike[] tmp = new FontStrike[slot+1];
                System.arraycopy(strikeSlots, 0, tmp, 0, strikeSlots.length);
                strikeSlots = tmp;
            }
            if (strikeSlots[slot] == null) {
                FontResource slotResource = fontResource.getSlotResource(slot);
                if (slotResource != null) {
                    strikeSlots[slot] = slotResource.getStrike(size, transform,
                                                               getAAMode());
                }
            }
            return strikeSlots[slot];
        }
    }

    @Override
    public FontResource getFontResource() {
        return fontResource;
    }

    public int getStrikeSlotForGlyph(int glyphCode) {
        return (glyphCode >>> 24);
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public boolean drawAsShapes() {
        return getStrikeSlot(0).drawAsShapes();
    }

    private PrismMetrics metrics;

    @Override
    public Metrics getMetrics() {
        if (metrics == null) {
            PrismFontFile fr = (PrismFontFile)fontResource.getSlotResource(0);
            metrics = fr.getFontMetrics(size);
        }
        return metrics;
    }

    @Override
    public Glyph getGlyph(char symbol) {
        int glyphCode = fontResource.getGlyphMapper().charToGlyph(symbol);
        return getGlyph(glyphCode);
    }

    @Override
    public Glyph getGlyph(int glyphCode) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        return getStrikeSlot(slot).getGlyph(slotglyphCode);
    }

     /**
     * Access to individual character advances are frequently needed for layout
     * understand that advance may vary for single glyph if ligatures or kerning
     * are enabled
     * @param ch char
     * @return advance of single char
     */
    @Override
    public float getCharAdvance(char ch) {
        int glyphCode = fontResource.getGlyphMapper().charToGlyph((int)ch);
        return fontResource.getAdvance(glyphCode, size);
    }

    @Override
    public int getQuantizedPosition(Vector2f point) {
        return getStrikeSlot(0).getQuantizedPosition(point);
    }

    @Override
    public Shape getOutline(GlyphList gl, BaseTransform transform) {

//        Path2D result = new Path2D();
//        getOutline(gl, transform, result);
//        return result;
        return null;
    }

    void getOutline(GlyphList gl, BaseTransform transform, Path2D p) {
        p.reset();
        if (gl == null) {
            return;
        }
        if (transform == null) {
            transform = BaseTransform.IDENTITY_TRANSFORM;
        }
//        Affine2D t = new Affine2D();
//        for (int i = 0; i < gl.getGlyphCount(); i++) {
//            int glyphCode = gl.getGlyphCode(i);
//            if (glyphCode != CharToGlyphMapper.INVISIBLE_GLYPH_ID) {
//                Glyph glyph = getGlyph(glyphCode);
//                Shape gp = glyph.getShape();
//                if (gp != null) {
//                    t.setTransform(transform);
//                    t.translate(gl.getPosX(i), gl.getPosY(i));
//                    p.append(gp.getPathIterator(t), false);
//                }
//            }
//        }
    }
}