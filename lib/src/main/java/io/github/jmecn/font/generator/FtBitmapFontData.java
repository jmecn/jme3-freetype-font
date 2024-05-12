package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;
import io.github.jmecn.font.freetype.FtFace;
import io.github.jmecn.font.freetype.FtLibrary;
import io.github.jmecn.font.freetype.FtStroker;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.TextureRegion;

import java.util.List;

import static org.lwjgl.util.freetype.FreeType.FT_KERNING_DEFAULT;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2024/5/12
 */
public class FtBitmapFontData extends BitmapFontData implements AutoCloseable {
    public List<TextureRegion> regions;

    // Fields for incremental glyph generation.
    FtFontGenerator generator;
    FtFontParameter parameter;
    FtStroker stroker;
    Packer packer;
    List<Glyph> glyphs;
    private boolean dirty;

    @Override
    public Glyph getGlyph(char ch) {
        Glyph glyph = super.getGlyph(ch);
        if (glyph == null && generator != null) {
            generator.setPixelSizes(0, parameter.size);
            float baseline = ((flipped ? -ascent : ascent) + capHeight) / scaleY;
            glyph = generator.createGlyph(ch, this, parameter, stroker, baseline, packer);
            if (glyph == null) return missingGlyph;

            setGlyphRegion(glyph, regions.get(glyph.getPage()));
            setGlyph(ch, glyph);
            glyphs.add(glyph);
            dirty = true;

            FtFace face = generator.face;
            if (parameter.kerning) {
                int glyphIndex = face.getCharIndex(ch);
                for (int i = 0, n = glyphs.size(); i < n; i++) {
                    BitmapCharacter other = glyphs.get(i);
                    int otherIndex = face.getCharIndex(other.getChar());

                    long kerning = face.getKerning(glyphIndex, otherIndex, FT_KERNING_DEFAULT);
                    if (kerning != 0) glyph.addKerning(other.getChar(), FtLibrary.toInt(kerning));

                    kerning = face.getKerning(otherIndex, glyphIndex, FT_KERNING_DEFAULT);
                    if (kerning != 0) other.addKerning(ch, FtLibrary.toInt(kerning));
                }
            }
        }
        return glyph;
    }

    @Override
    public void getGlyphs (GlyphRun run, CharSequence str, int start, int end, Glyph lastGlyph) {
        if (packer != null) packer.setPackToTexture(true); // All glyphs added after this are packed directly to the texture.
        super.getGlyphs(run, str, start, end, lastGlyph);
        if (dirty) {
            dirty = false;
            packer.updateTextureRegions(regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
        }
    }

    @Override
    public void close() {
        if (stroker != null) stroker.close();
        if (packer != null) packer.close();
    }
}
