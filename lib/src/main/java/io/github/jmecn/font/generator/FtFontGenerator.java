package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.image.ImageRaster;
import io.github.jmecn.font.FtBitmapFont;
import io.github.jmecn.font.freetype.*;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Rectangle;
import io.github.jmecn.font.packer.TextureRegion;
import io.github.jmecn.font.packer.strategy.GuillotineStrategy;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;
import io.github.jmecn.font.utils.ImageUtils;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.FT_KERNING_DEFAULT;

/**
 * FreeType font generator
 *
 * @author yanmaoyuan
 */
public class FtFontGenerator implements AutoCloseable {
    static Logger logger = LoggerFactory.getLogger(FtFontGenerator.class);

    public static final int MAX_SIZE = 1024;

    FtLibrary library;
    FtFace face;
    boolean bitmapped;
    private String name;
    private int pixelWidth;
    private int pixelHeight;

    public FtFontGenerator(File file) {
        this(file, 0);
    }

    public FtFontGenerator(File file, int faceIndex) {
        library = new FtLibrary();
        face = library.newFace(file, faceIndex);
        name = file.getName();// FIXME with out extension
        if (checkForBitmapFont()) {
            return;
        }
        setPixelSizes(0, 15);
    }

    @Override
    public void close() throws Exception {
        face.close();
        library.close();
    }

    private boolean checkForBitmapFont () {
        if (face.hasFixedSizes() && face.hasHorizontal() && face.loadChar(0x20)) {
            FtGlyphSlot slot = face.getGlyph();
            if (slot.getFormat() == 0x62697473) {// 'bits'
                bitmapped = true;
            }
        }

        return bitmapped;
    }

    public void setPixelSizes(int pixelWidth, int pixelHeight) {
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        if (!bitmapped && !face.setPixelSize(pixelWidth, pixelHeight)) {
            throw new FtRuntimeException("Can't set pixel size for font");
        }
    }

    public FtBitmapFont generateFont(FtFontParameter parameter) {
        return generateFont(parameter, new FtBitmapCharacterSet());
    }

    public FtBitmapFont generateFont(FtFontParameter parameter, FtBitmapCharacterSet data) {
        boolean updateTextureRegions = data.regions == null && parameter.packer != null;
        if (updateTextureRegions) {
            data.regions = new ArrayList<>();
        }

        generateData(parameter, data);
        if (updateTextureRegions)
            parameter.packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
        if (data.regions.isEmpty()) throw new FtRuntimeException("Unable to create a font with no texture regions.");
        FtBitmapFont font = newBitmapFont(data, data.regions, true);
        font.setOwnsTexture(parameter.packer == null);
        return font;
    }

    protected FtBitmapFont newBitmapFont(FtBitmapCharacterSet data, List<TextureRegion> pageRegions, boolean ownsTexture) {
        return new FtBitmapFont(data, pageRegions, ownsTexture);
    }

    public FtBitmapCharacterSet generateData(int size) {
        FtFontParameter parameter = new FtFontParameter();
        parameter.size = size;
        return generateData(parameter);
    }

    public FtBitmapCharacterSet generateData(FtFontParameter parameter) {
        return generateData(parameter, new FtBitmapCharacterSet());
    }

    public FtBitmapCharacterSet generateData(FtFontParameter parameter, FtBitmapCharacterSet data) {
        data.name = name + "-" + parameter.size;
        char[] characters = parameter.characters.toCharArray();
        int charactersLength = characters.length;
        boolean incremental = parameter.incremental;
        int flags = parameter.hinting.getLoadFlags();

        setPixelSizes(0, parameter.size);

        // set general font data
        FtSizeMetrics fontMetrics = face.getSize().getMetrics();
        data.setRenderedSize(parameter.size);
        data.setFlip(parameter.flip);
        data.setAscent(FtLibrary.from26D6ToInt(fontMetrics.getAscender()));
        data.setDescent(FtLibrary.from26D6ToInt(fontMetrics.getDescender()));
        data.setLineHeight(FtLibrary.from26D6ToInt(fontMetrics.getHeight()));
        float baseLine = data.getAscent();

        // if bitmapped
        if (bitmapped && (data.getLineHeight() == 0)) {
            for (int c = 32; c < (32 + face.getNumGlyphs()); c++) {
                if (face.loadChar(c, flags)) {
                    int lh = FtLibrary.from26D6ToInt(face.getGlyph().getMetrics().getHeight());
                    if (lh > data.getLineHeight()) {
                        data.setLineHeight(lh);
                    }
                }
            }
        }
        data.lineHeight += parameter.spaceY;

        // determine space width
        if (face.loadChar(' ', flags) || face.loadChar('l', flags)) {
            data.spaceXadvance = FtLibrary.from26D6ToInt(face.getGlyph().getMetrics().getHoriAdvance());
        } else {
            data.spaceXadvance = face.getMaxAdvanceWidth(); // Possibly very wrong.
        }

        // determine x-height
        for (char xChar : data.xChars) {
            if (face.loadChar(xChar, flags)) {
                data.xHeight = FtLibrary.from26D6ToInt(face.getGlyph().getMetrics().getHeight());
                break;
            }
        }
        if (data.xHeight == 0) throw new FtRuntimeException("No x-height character found in font");

        // determine cap height
        for (char capChar : data.capChars) {
            if (face.loadChar(capChar, flags)) {
                data.capHeight = FtLibrary.from26D6ToInt(face.getGlyph().getMetrics().getHeight()) + Math.abs(parameter.shadowOffsetY);
                break;
            }
        }
        if (!bitmapped && data.capHeight == 1) throw new FtRuntimeException("No cap character found in font");

        data.ascent -= data.capHeight;
        data.down = -data.lineHeight;
        if (parameter.flip) {
            data.ascent = -data.ascent;
            data.down = -data.down;
        }

        boolean ownsAtlas = false;

        Packer packer = parameter.packer;

        if (packer == null) {
            // Create a packer.
            int size;
            PackStrategy packStrategy;
            if (incremental) {
                size = MAX_SIZE;
                packStrategy = new GuillotineStrategy();
            } else {
                int maxGlyphHeight = (int)Math.ceil(data.lineHeight);
                size = FastMath.nearestPowerOfTwo((int)Math.sqrt(maxGlyphHeight * maxGlyphHeight * charactersLength));
                if (MAX_SIZE > 0) {
                    size = Math.min(size, MAX_SIZE);
                }
                packStrategy = new SkylineStrategy();
            }
            ownsAtlas = true;
            packer = new Packer(Image.Format.RGBA8, size, size, 1, false, packStrategy);
            packer.setTransparentColor(parameter.color);
            packer.getTransparentColor().a = 0;
            if (parameter.borderWidth > 0) {
                packer.setTransparentColor(parameter.borderColor);
                packer.getTransparentColor().a = 0;
            }

            parameter.packer = packer;
        }

        if (incremental) data.glyphs = new ArrayList<>(charactersLength + 32);

        FtStroker stroker = null;
        if (parameter.borderWidth > 0) {
            stroker = library.newStroker();
            stroker.set((int)(parameter.borderWidth * 64f),
                    parameter.borderStraight ? FreeType.FT_STROKER_LINECAP_BUTT : FreeType.FT_STROKER_LINECAP_ROUND,
                    parameter.borderStraight ? FreeType.FT_STROKER_LINEJOIN_MITER_FIXED : FreeType.FT_STROKER_LINEJOIN_ROUND, 0);
        }

        // Create glyphs largest height first for best packing.
        int[] heights = new int[charactersLength];
        for (int i = 0; i < charactersLength; i++) {
            char c = characters[i];

            int height = face.loadChar(c, flags) ? FtLibrary.from26D6ToInt(face.getGlyph().getMetrics().getHeight()) : 0;
            heights[i] = height;

            if (c == '\0') {
                Glyph missingGlyph = createGlyph('\0', data, parameter, stroker, baseLine, packer);
                if (missingGlyph != null && missingGlyph.getWidth() != 0 && missingGlyph.getHeight() != 0) {
                    data.addCharacter('\0', missingGlyph);
                    data.missingGlyph = missingGlyph;
                    if (incremental) data.glyphs.add(missingGlyph);
                }
            }
        }
        int heightsCount = heights.length;
        while (heightsCount > 0) {
            int best = 0;
            int maxHeight = heights[0];
            for (int i = 1; i < heightsCount; i++) {
                int height = heights[i];
                if (height > maxHeight) {
                    maxHeight = height;
                    best = i;
                }
            }

            char c = characters[best];
            if (data.getCharacter(c) == null) {
                Glyph glyph = createGlyph(c, data, parameter, stroker, baseLine, packer);
                if (glyph != null) {
                    data.addCharacter(c, glyph);
                    if (incremental) data.glyphs.add(glyph);
                }
            }

            heightsCount--;
            heights[best] = heights[heightsCount];
            char tmpChar = characters[best];
            characters[best] = characters[heightsCount];
            characters[heightsCount] = tmpChar;
        }

        if (stroker != null && !incremental) {
            stroker.close();
        }

        if (incremental) {
            data.generator = this;
            data.parameter = parameter;
            data.stroker = stroker;
            data.packer = packer;
        }

        // Generate kerning.
        parameter.kerning &= face.hasKerning();
        if (parameter.kerning) {
            for (int i = 0; i < charactersLength; i++) {
                char firstChar = characters[i];
                BitmapCharacter first = data.getCharacter(firstChar);
                if (first == null) continue;
                int firstIndex = face.getCharIndex(firstChar);
                for (int ii = i; ii < charactersLength; ii++) {
                    char secondChar = characters[ii];
                    BitmapCharacter second = data.getCharacter(secondChar);
                    if (second == null) continue;
                    int secondIndex = face.getCharIndex(secondChar);

                    long kerning = face.getKerning(firstIndex, secondIndex, FT_KERNING_DEFAULT); // FT_KERNING_DEFAULT (scaled then rounded).
                    if (kerning != 0) first.addKerning(secondChar, FtLibrary.from26D6ToInt(kerning));

                    kerning = face.getKerning(secondIndex, firstIndex, FT_KERNING_DEFAULT); // FT_KERNING_DEFAULT (scaled then rounded).
                    if (kerning != 0) second.addKerning(firstChar, FtLibrary.from26D6ToInt(kerning));
                }
            }
        }

        // Generate texture regions.
        if (ownsAtlas) {
            data.regions = new ArrayList<>();
            packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
        }

        // Set space glyph.
        Glyph spaceGlyph = data.getCharacter(' ');
        if (spaceGlyph == null) {
            spaceGlyph = new Glyph();
            spaceGlyph.setXAdvance( (int)data.spaceXadvance + parameter.spaceX );
            spaceGlyph.setChar(' ');
            data.addCharacter(' ', spaceGlyph);
        }
        if (spaceGlyph.getWidth() == 0) {
            spaceGlyph.setWidth( (int)(spaceGlyph.getXAdvance() + data.padRight) );
        }

        return data;
    }

    /** @return null if glyph was not found. */
    protected Glyph createGlyph(char c,
                                          FtBitmapCharacterSet data,
                                          FtFontParameter parameter,
                                          FtStroker stroker,
                                          float baseLine,
                                          Packer packer) {

        boolean missing = face.getCharIndex(c) == 0 && c != 0;
        if (missing) return null;

        if (!face.loadChar(c, parameter.hinting.getLoadFlags())) return null;

        FtGlyphSlot slot = face.getGlyph();
        FtGlyph main = slot.getGlyph();
        FtBitmapGlyph mainGlyph;
        try {
            mainGlyph = main.toBitmap(parameter.mono ? FreeType.FT_RENDER_MODE_MONO : FreeType.FT_RENDER_MODE_NORMAL);
        } catch (FtRuntimeException e) {
            main.close();
            logger.error("Couldn't render char: {}", c, e);
            return null;
        }
        FtBitmap mainBitmap = mainGlyph.getBitmap();
        Image mainPixmap = mainBitmap.getPixmap(Image.Format.RGBA8, parameter.color, parameter.gamma);

        if (mainBitmap.getWidth() != 0 && mainBitmap.getRows() != 0) {
            long offsetX = 0;
            long offsetY = 0;
            if (parameter.borderWidth > 0) {
                // execute stroker; this generates a glyph "extended" along the outline
                long top = mainGlyph.getTop();
                long left = mainGlyph.getLeft();
                FtGlyph border = slot.getGlyph();
                border = border.strokeBorder(stroker, false, true);
                FtBitmapGlyph borderGlyph = border.toBitmap(parameter.mono ? FreeType.FT_RENDER_MODE_MONO : FreeType.FT_RENDER_MODE_NORMAL);
                offsetX = left - borderGlyph.getLeft();
                offsetY = -(top - borderGlyph.getTop());

                // Render border (pixmap is bigger than main).
                FtBitmap borderBitmap = borderGlyph.getBitmap();
                Image borderPixmap = borderBitmap.getPixmap(Image.Format.RGBA8, parameter.borderColor, parameter.borderGamma);

                // Draw main glyph on top of border.
                for (int i = 0, n = parameter.renderCount; i < n; i++) {
                    ImageUtils.drawImage(borderPixmap, mainPixmap, (int) offsetX, (int) offsetY);
                }

                mainPixmap.dispose();
                mainGlyph.close();
                mainPixmap = borderPixmap;
                mainGlyph = borderGlyph;
            }

            if (parameter.shadowOffsetX != 0 || parameter.shadowOffsetY != 0) {
                int mainW = mainPixmap.getWidth();
                int mainH = mainPixmap.getHeight();
                int shadowOffsetX = Math.max(parameter.shadowOffsetX, 0);
                int shadowOffsetY = Math.max(parameter.shadowOffsetY, 0);
                int shadowW = mainW + Math.abs(parameter.shadowOffsetX);
                int shadowH = mainH + Math.abs(parameter.shadowOffsetY);
                Image shadowPixmap = ImageUtils.newImage(mainPixmap.getFormat(), shadowW, shadowH);

                ColorRGBA shadowColor = parameter.shadowColor;
                float a = shadowColor.a;
                if (a != 0) {
                    byte r = (byte)(shadowColor.r * 255), g = (byte)(shadowColor.g * 255), b = (byte)(shadowColor.b * 255);
                    ByteBuffer mainPixels = mainPixmap.getData(0);
                    ByteBuffer shadowPixels = shadowPixmap.getData(0);
                    for (int y = 0; y < mainH; y++) {
                        int shadowRow = shadowW * (y + shadowOffsetY) + shadowOffsetX;
                        for (int x = 0; x < mainW; x++) {
                            int mainPixel = (mainW * y + x) * 4;
                            byte mainA = mainPixels.get(mainPixel + 3);
                            if (mainA == 0) continue;
                            int shadowPixel = (shadowRow + x) * 4;
                            shadowPixels.put(shadowPixel, r);
                            shadowPixels.put(shadowPixel + 1, g);
                            shadowPixels.put(shadowPixel + 2, b);
                            shadowPixels.put(shadowPixel + 3, (byte)((mainA & 0xff) * a));
                        }
                    }
                }

                // Draw main glyph (with any border) on top of shadow.
                for (int i = 0, n = parameter.renderCount; i < n; i++) {
                    ImageUtils.drawImage(shadowPixmap, mainPixmap, Math.max(-parameter.shadowOffsetX, 0), Math.max(-parameter.shadowOffsetY, 0));
                }
                mainPixmap.dispose();
                mainPixmap = shadowPixmap;
            } else if (parameter.borderWidth == 0) {
                // No shadow and no border, draw glyph additional times.
                for (int i = 0, n = parameter.renderCount - 1; i < n; i++) {
                    ImageUtils.drawImage(mainPixmap, mainPixmap, 0, 0);
                }
            }

            if (parameter.padTop > 0 || parameter.padLeft > 0 || parameter.padBottom > 0 || parameter.padRight > 0) {
                Image padPixmap = ImageUtils.newImage(mainPixmap.getFormat(), mainPixmap.getWidth() + parameter.padLeft + parameter.padRight,
                        mainPixmap.getHeight() + parameter.padTop + parameter.padBottom);
                ImageUtils.drawImage(padPixmap, mainPixmap, parameter.padLeft, parameter.padTop);
                mainPixmap.dispose();
                mainPixmap = padPixmap;
            }
        }

        FtGlyphMetrics metrics = slot.getMetrics();
        Glyph glyph = new Glyph(c);
        glyph.setWidth(mainPixmap.getWidth());
        glyph.setHeight(mainPixmap.getHeight());
        glyph.setXOffset(mainGlyph.getLeft());// FIXME should << 6 ?
        if (parameter.flip) {
            glyph.setYOffset(-mainGlyph.getTop() + (int) baseLine);// FIXME should << 6 ?
        }
        else {
            glyph.setYOffset(-(glyph.getHeight() - mainGlyph.getTop()) - (int) baseLine);// FIXME should << 6 ?
        }
        glyph.setXAdvance( FtLibrary.from26D6ToInt(metrics.getHoriAdvance()) + (int)parameter.borderWidth + parameter.spaceX );
        glyph.setFixedWidth(face.isFixedWidth());

        if (bitmapped) {
            ImageRaster raster = ImageRaster.create(mainPixmap);
            ByteBuffer buf = mainBitmap.getBuffer();
            for (int h = 0; h < glyph.getHeight(); h++) {
                int idx = h * Math.abs(mainBitmap.getPitch());
                for (int w = 0; w < (glyph.getWidth() + glyph.getXOffset()); w++) {
                    int bit = (buf.get(idx + (w / 8)) >>> (7 - (w % 8))) & 1;
                    raster.setPixel(w, h, ((bit == 1) ? ColorRGBA.White : ColorRGBA.BlackNoAlpha));
                }
            }
        }

        String pixmapName = glyph.hashCode() + "_" + glyph.getChar();
        Rectangle rect = packer.pack(pixmapName, mainPixmap);
        glyph.setPage(packer.getPageIndex(pixmapName));
        glyph.setX(rect.getX());
        glyph.setY(rect.getY());

        // If a page was added, create a new texture region for the incrementally added glyph.
        if (parameter.incremental && data.regions != null && data.regions.size() <= glyph.getPage()) {
            packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
        }

        mainPixmap.dispose();
        mainGlyph.close();

        return glyph;
    }
}
