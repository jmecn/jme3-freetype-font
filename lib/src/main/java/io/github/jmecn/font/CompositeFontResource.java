package io.github.jmecn.font;

public interface CompositeFontResource extends FontResource {

    FontResource getSlotResource(int slot);

    int getNumSlots();

    default int addSlotFont(FontResource font) {
        return -1;
    }

    /**
     * Returns the slot for the given font name.
     * Adds fontName as a new fallback font if needed.
     */
    public int getSlotForFont(String fontName);

    default boolean isColorGlyph(int glyphCode) {
        int slot = (glyphCode >>> 24);
        int slotglyphCode = glyphCode & CompositeGlyphMapper.GLYPHMASK;
        FontResource slotResource = getSlotResource(slot);
        return slotResource.isColorGlyph(slotglyphCode);
    }
}