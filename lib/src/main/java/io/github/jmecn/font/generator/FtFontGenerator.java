package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.image.ImageRaster;
import io.github.jmecn.font.FtBitmapCharacterSet;
import io.github.jmecn.font.FtBitmapFont;
import io.github.jmecn.font.Glyph;
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

import static org.lwjgl.util.freetype.FreeType.*;

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

    public FtFace getFace() {
        return face;
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
        boolean updateTextureRegions = data.regions == null && parameter.getPacker() != null;
        if (updateTextureRegions) {
            data.regions = new ArrayList<>();
        }

        generateData(parameter, data);
        if (updateTextureRegions) {
            parameter.getPacker().updateTextureRegions(data.regions, parameter.getMinFilter(), parameter.getMagFilter(), parameter.isGenMipMaps());
        }
        if (data.regions.isEmpty()) throw new FtRuntimeException("Unable to create a font with no texture regions.");
        FtBitmapFont font = newBitmapFont(data, data.regions, true);
        font.setOwnsTexture(parameter.getPacker() == null);
        return font;
    }

    protected FtBitmapFont newBitmapFont(FtBitmapCharacterSet data, List<TextureRegion> pageRegions, boolean ownsTexture) {
        return new FtBitmapFont(data, pageRegions, ownsTexture);
    }

    public FtBitmapCharacterSet generateData(int size) {
        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(size);
        return generateData(parameter);
    }

    public FtBitmapCharacterSet generateData(FtFontParameter parameter) {
        return generateData(parameter, new FtBitmapCharacterSet());
    }

    public FtBitmapCharacterSet generateData(FtFontParameter parameter, FtBitmapCharacterSet data) {
        data.name = name + "-" + parameter.getSize();
        char[] characters = parameter.getCharacters().toCharArray();
        int charactersLength = characters.length;
        boolean incremental = parameter.isIncremental();
        int flags = parameter.getLoadFlags();

        setPixelSizes(0, parameter.getSize());

        boolean ownsAtlas = false;
        Packer packer;
        if (parameter.getPacker() == null) {
            packer = newPacker(parameter, data, charactersLength);
            ownsAtlas = true;
        } else {
            packer = parameter.getPacker();
        }

        // set general font data
        FtSizeMetrics fontMetrics = face.getSize().getMetrics();
        data.setRenderedSize(parameter.getSize());
        data.setFlip(parameter.isFlip());
        data.setAscent(FtLibrary.from26D6(fontMetrics.getAscender()));
        data.setDescent(FtLibrary.from26D6(fontMetrics.getDescender()));
        data.setLineHeight(FtLibrary.from26D6(fontMetrics.getHeight()));
        float baseLine = data.getAscent();

        // if bitmapped
        if (bitmapped && (data.getLineHeight() == 0)) {
            for (int c = 32; c < (32 + face.getNumGlyphs()); c++) {
                if (face.loadChar(c, flags)) {
                    int lh = FtLibrary.from26D6(face.getGlyph().getMetrics().getHeight());
                    if (lh > data.getLineHeight()) {
                        data.setLineHeight(lh);
                    }
                }
            }
        }
        data.lineHeight += parameter.getSpaceY();

        // determine space width
        if (face.loadChar(' ', flags) || face.loadChar('l', flags)) {
            data.spaceXadvance = FtLibrary.from26D6(face.getGlyph().getMetrics().getHoriAdvance());
        } else {
            data.spaceXadvance = face.getMaxAdvanceWidth(); // Possibly very wrong.
        }

        // determine x-height
        for (char xChar : data.xChars) {
            if (face.loadChar(xChar, flags)) {
                data.xHeight = FtLibrary.from26D6(face.getGlyph().getMetrics().getHeight());
                break;
            }
        }
        if (data.xHeight == 0) throw new FtRuntimeException("No x-height character found in font");

        // determine cap height
        for (char capChar : data.capChars) {
            if (face.loadChar(capChar, flags)) {
                data.capHeight = FtLibrary.from26D6(face.getGlyph().getMetrics().getHeight()) + Math.abs(parameter.getShadowOffsetY());
                break;
            }
        }
        if (!bitmapped && data.capHeight == 1) throw new FtRuntimeException("No cap character found in font");

        data.ascent -= data.capHeight;
        data.down = -data.lineHeight;
        if (parameter.isFlip()) {
            data.ascent = -data.ascent;
            data.down = -data.down;
        }

        FtStroker stroker = null;
        if (parameter.getBorderWidth() > 0) {
            stroker = library.newStroker();
            stroker.set((int)(parameter.getBorderWidth() * 64f),
                    parameter.isBorderStraight() ? FreeType.FT_STROKER_LINECAP_BUTT : FreeType.FT_STROKER_LINECAP_ROUND,
                    parameter.isBorderStraight() ? FreeType.FT_STROKER_LINEJOIN_MITER_FIXED : FreeType.FT_STROKER_LINEJOIN_ROUND,
                    0);
        }

        // Create glyphs largest height first for best packing.
        int[] heights = new int[charactersLength];
        for (int i = 0; i < charactersLength; i++) {
            char c = characters[i];

            int height = face.loadChar(c, flags) ? FtLibrary.from26D6(face.getGlyph().getMetrics().getHeight()) : 0;
            heights[i] = height;

            if (c == '\0') {
                Glyph missingGlyph = createGlyph('\0', data, parameter, stroker, baseLine, packer);
                if (missingGlyph != null && missingGlyph.getWidth() != 0 && missingGlyph.getHeight() != 0) {
                    data.addCharacter('\0', missingGlyph);
                    data.missingGlyph = missingGlyph;
                    if (incremental) {
                        data.getGlyphs().add(missingGlyph);
                    }
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
                    if (incremental) {
                        data.getGlyphs().add(glyph);
                    }
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
            data.setGenerator(this);
            data.setParameter(parameter);
            data.setStroker(stroker);
            data.setPacker(packer);
        }

        // Generate kerning.
        parameter.setKerning(parameter.isKerning() & face.hasKerning());
        generateKerning(parameter, data, characters, charactersLength);

        // Generate texture regions.
        if (ownsAtlas) {
            data.regions = new ArrayList<>();
            packer.updateTextureRegions(data.regions, parameter.getMinFilter(), parameter.getMagFilter(), parameter.isGenMipMaps());
        }

        // Set space glyph.
        Glyph spaceGlyph = data.getCharacter(' ');
        if (spaceGlyph == null) {
            spaceGlyph = new Glyph();
            spaceGlyph.setXAdvance( (int)data.spaceXadvance + parameter.getSpaceX() );
            spaceGlyph.setChar(' ');
            data.addCharacter(' ', spaceGlyph);
        }
        if (spaceGlyph.getWidth() == 0) {
            spaceGlyph.setWidth( (int)(spaceGlyph.getXAdvance() + data.padRight) );
        }

        return data;
    }

    // Create a packer.
    private Packer newPacker(FtFontParameter parameter, FtBitmapCharacterSet data, int charactersLength) {
        int size;
        PackStrategy packStrategy;
        if (parameter.isIncremental()) {
            size = MAX_SIZE;
            packStrategy = new GuillotineStrategy();
        } else {
            int maxGlyphHeight = data.getLineHeight();
            size = FastMath.nearestPowerOfTwo((int)Math.sqrt(maxGlyphHeight * maxGlyphHeight * (double) charactersLength));
            if (MAX_SIZE > 0) {
                size = Math.min(size, MAX_SIZE);
            }
            packStrategy = new SkylineStrategy();
        }
        Packer packer = new Packer(Image.Format.RGBA8, size, size, 1, false, packStrategy);
        packer.setTransparentColor(parameter.getColor());
        packer.getTransparentColor().a = 0;
        if (parameter.getBorderWidth() > 0) {
            packer.setTransparentColor(parameter.getBorderColor());
            packer.getTransparentColor().a = 0;
        }

        return packer;
    }

    public void generateKerning(FtFontParameter parameter, FtBitmapCharacterSet data, char[] characters, int charactersLength) {
        if (parameter.isKerning()) {
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
                    if (kerning != 0) first.addKerning(secondChar, FtLibrary.from26D6(kerning));

                    kerning = face.getKerning(secondIndex, firstIndex, FT_KERNING_DEFAULT); // FT_KERNING_DEFAULT (scaled then rounded).
                    if (kerning != 0) second.addKerning(firstChar, FtLibrary.from26D6(kerning));
                }
            }
        }
    }

    /** @return null if glyph was not found. */
    public Glyph createGlyph(char c, FtBitmapCharacterSet data, FtFontParameter parameter, FtStroker stroker,
                             float baseLine, Packer packer) {

        boolean missing = face.getCharIndex(c) == 0 && c != 0;
        if (missing) {
            return null;
        }

        if (!face.loadChar(c, parameter.getLoadFlags())) {
            return null;
        }

        FtGlyphSlot slot = face.getGlyph();
        FtGlyph main = slot.getGlyph();
        FtBitmapGlyph mainGlyph;
        try {
            mainGlyph = main.toBitmap(parameter.getRenderMode());
        } catch (FtRuntimeException e) {
            main.close();
            logger.error("Couldn't render char: {}", c, e);
            return null;
        }
        FtBitmap mainBitmap = mainGlyph.getBitmap();
        Image mainImage = mainBitmap.getImage(Image.Format.RGBA8, parameter.getColor(), parameter.getGamma());

        if (mainBitmap.getWidth() != 0 && mainBitmap.getRows() != 0) {
            long offsetX;
            long offsetY;
            if (parameter.getBorderWidth() > 0) {
                // execute stroker; this generates a glyph "extended" along the outline
                long top = mainGlyph.getTop();
                long left = mainGlyph.getLeft();
                FtGlyph border = slot.getGlyph();
                border = border.strokeBorder(stroker, false, true);
                FtBitmapGlyph borderGlyph = border.toBitmap(parameter.getRenderMode());
                offsetX = left - borderGlyph.getLeft();
                offsetY = -(top - borderGlyph.getTop());

                // Render border (pixmap is bigger than main).
                FtBitmap borderBitmap = borderGlyph.getBitmap();
                Image borderPixmap = borderBitmap.getImage(Image.Format.RGBA8, parameter.getBorderColor(), parameter.getBorderGamma());

                // Draw main glyph on top of border.
                for (int i = 0, n = parameter.getRenderCount(); i < n; i++) {
                    ImageUtils.drawImage(borderPixmap, mainImage, (int) offsetX, (int) offsetY);
                }

                mainImage.dispose();
                mainGlyph.close();
                mainImage = borderPixmap;
                mainGlyph = borderGlyph;
            }

            if (parameter.getShadowOffsetX() != 0 || parameter.getShadowOffsetY() != 0) {
                int mainW = mainImage.getWidth();
                int mainH = mainImage.getHeight();
                int shadowOffsetX = Math.max(parameter.getShadowOffsetX(), 0);
                int shadowOffsetY = Math.max(parameter.getShadowOffsetY(), 0);
                int shadowW = mainW + Math.abs(parameter.getShadowOffsetX());
                int shadowH = mainH + Math.abs(parameter.getShadowOffsetY());
                Image shadowPixmap = ImageUtils.newImage(mainImage.getFormat(), shadowW, shadowH);

                ColorRGBA shadowColor = parameter.getShadowColor();
                float a = shadowColor.a;
                if (a != 0) {
                    byte r = (byte)(shadowColor.r * 255);
                    byte g = (byte)(shadowColor.g * 255);
                    byte b = (byte)(shadowColor.b * 255);
                    ByteBuffer mainPixels = mainImage.getData(0);
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
                for (int i = 0, n = parameter.getRenderCount(); i < n; i++) {
                    ImageUtils.drawImage(shadowPixmap, mainImage, Math.max(-parameter.getShadowOffsetX(), 0), Math.max(-parameter.getShadowOffsetY(), 0));
                }
                mainImage.dispose();
                mainImage = shadowPixmap;
            } else if (parameter.getBorderWidth() == 0) {
                // No shadow and no border, draw glyph additional times.
                for (int i = 0, n = parameter.getRenderCount() - 1; i < n; i++) {
                    ImageUtils.drawImage(mainImage, mainImage, 0, 0);
                }
            }

            if (parameter.getPadTop() > 0 || parameter.getPadLeft() > 0 || parameter.getPadBottom() > 0 || parameter.getPadRight() > 0) {
                Image padPixmap = ImageUtils.newImage(mainImage.getFormat(), mainImage.getWidth() + parameter.getPadLeft() + parameter.getPadRight(),
                        mainImage.getHeight() + parameter.getPadTop() + parameter.getPadBottom());
                ImageUtils.drawImage(padPixmap, mainImage, parameter.getPadLeft(), parameter.getPadRight());
                mainImage.dispose();
                mainImage = padPixmap;
            }
        }

        FtGlyphMetrics metrics = slot.getMetrics();
        Glyph glyph = new Glyph(c);
        glyph.setWidth(mainImage.getWidth());
        glyph.setHeight(mainImage.getHeight());
        glyph.setXOffset(mainGlyph.getLeft());
        if (parameter.isFlip()) {
            glyph.setYOffset(-mainGlyph.getTop() + (int) baseLine);
        }
        else {
            glyph.setYOffset(-(glyph.getHeight() - mainGlyph.getTop()) - (int) baseLine);
        }
        glyph.setXAdvance( FtLibrary.from26D6(metrics.getHoriAdvance()) + (int)parameter.getBorderWidth() + parameter.getSpaceX() );
        glyph.setFixedWidth(face.isFixedWidth());

        if (bitmapped) {
            ImageRaster raster = ImageRaster.create(mainImage);
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
        Rectangle rect = packer.pack(pixmapName, mainImage);
        glyph.setPage(packer.getPageIndex(pixmapName));
        glyph.setX(rect.getX());
        glyph.setY(rect.getY());

        // If a page was added, create a new texture region for the incrementally added glyph.
        if (parameter.isIncremental() && data.regions != null && data.regions.size() <= glyph.getPage()) {
            packer.updateTextureRegions(data.regions, parameter.getMinFilter(), parameter.getMagFilter(), parameter.isGenMipMaps());
        }

        mainImage.dispose();
        mainGlyph.close();

        return glyph;
    }
}
