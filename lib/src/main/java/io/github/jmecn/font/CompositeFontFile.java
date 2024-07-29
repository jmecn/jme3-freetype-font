package io.github.jmecn.font;

public interface CompositeFontFile extends FontFile {

    FontFile getSlotResource(int slot);

    int getNumSlots();

    default int addSlotFont(FontFile font) {
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
        FontFile slotResource = getSlotResource(slot);
        return slotResource.isColorGlyph(slotglyphCode);
    }
}