package io.github.jmecn.font;

import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.util.IntMap;
import io.github.jmecn.font.generator.FtBitmapFontData;
import io.github.jmecn.font.generator.Glyph;
import io.github.jmecn.font.packer.TextureRegion;

import java.util.List;

/**
 * desc:
 */
public class FtBitmapFont extends BitmapFont {
    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
    private static final int PAGES = 0x10000 / PAGE_SIZE;

    FtBitmapFontData data;
    List<TextureRegion> regions;
    private boolean flipped;
    boolean integer;
    private boolean ownsTexture;

    private IntMap<Material> materials;

    public FtBitmapFont(FtBitmapFontData data, List<TextureRegion> pageRegions, boolean integer) {
        materials = new IntMap<>();

        this.flipped = data.flipped;
        this.data = data;
        this.integer = integer;

        regions = pageRegions;
        ownsTexture = false;

        load(data);
    }

    protected void load (FtBitmapFontData data) {
        for (Glyph glyph : data.getGlyphs()) {
            if (glyph != null) data.setGlyphRegion(glyph, regions.get(glyph.getPage()));
        }
        if (data.missingGlyph != null) {
            data.setGlyphRegion(data.missingGlyph, regions.get(data.missingGlyph.getPage()));
        }
    }

    /** @return whether the texture is owned by the font, font disposes the texture itself if true */
    public boolean ownsTexture () {
        return ownsTexture;
    }

    /**
     * Sets whether the font owns the texture. In case it does, the font will also dispose of the texture when {@link ()}
     * is called. Use with care!
     * @param ownsTexture whether the font owns the texture */
    public void setOwnsTexture (boolean ownsTexture) {
        this.ownsTexture = ownsTexture;
    }

    @Override
    public Material getPage(int page) {
        // FIXME get exists material, or create new material
        return materials.get(page);
    }
}
