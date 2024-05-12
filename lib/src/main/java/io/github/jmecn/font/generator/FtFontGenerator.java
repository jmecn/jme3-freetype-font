package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import io.github.jmecn.font.freetype.*;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.packer.PackStrategy;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.Rectangle;
import io.github.jmecn.font.packer.strategy.BiTreePackStrategy;
import io.github.jmecn.font.packer.strategy.ScanlinePackStrategy;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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

    /** Returns the next power of two. Returns the specified value if the value is already a power of two. */
    public static int nextPowerOfTwo(int value) {
        if (value == 0) return 1;
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
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

    public FtBitmapFontData generate(FtFontParameter parameter) {
        return generate(parameter, new FtBitmapFontData());
    }

    public FtBitmapFontData generate(FtFontParameter parameter, FtBitmapFontData data) {
        data.name = name + "-" + parameter.size;
        char[] characters = parameter.characters.toCharArray();
        int charactersLength = characters.length;
        boolean incremental = parameter.incremental;
        int flags = parameter.hinting.getLoadFlag();

        setPixelSizes(0, parameter.size);

        // set general font data
        FtSizeMetrics fontMetrics = face.getSize().getMetrics();
        data.flipped = parameter.flip;
        data.ascent = FtLibrary.toInt(fontMetrics.getAscender());
        data.descent = FtLibrary.toInt(fontMetrics.getDescender());
        data.lineHeight = FtLibrary.toInt(fontMetrics.getHeight());
        float baseLine = data.ascent;

        // if bitmapped
        if (bitmapped && (data.lineHeight == 0)) {
            for (int c = 32; c < (32 + face.getNumGlyphs()); c++) {
                if (face.loadChar(c, flags)) {
                    int lh = FtLibrary.toInt(face.getGlyph().getMetrics().getHeight());
                    data.lineHeight = (lh > data.lineHeight) ? lh : data.lineHeight;
                }
            }
        }
        data.lineHeight += parameter.spaceY;

        // determine space width
        if (face.loadChar(' ', flags) || face.loadChar('l', flags)) {
            data.spaceXadvance = FtLibrary.toInt(face.getGlyph().getMetrics().getHoriAdvance());
        } else {
            data.spaceXadvance = face.getMaxAdvanceWidth(); // Possibly very wrong.
        }

        // determine x-height
        for (char xChar : data.xChars) {
            if (!face.loadChar(xChar, flags)) continue;
            data.xHeight = FtLibrary.toInt(face.getGlyph().getMetrics().getHeight());
            break;
        }
        if (data.xHeight == 0) throw new FtRuntimeException("No x-height character found in font");

        // determine cap height
        for (char capChar : data.capChars) {
            if (!face.loadChar(capChar, flags)) continue;
            data.capHeight = FtLibrary.toInt(face.getGlyph().getMetrics().getHeight()) + Math.abs(parameter.shadowOffsetY);
            break;
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
                packStrategy = new BiTreePackStrategy();
            } else {
                int maxGlyphHeight = (int)Math.ceil(data.lineHeight);
                size = nextPowerOfTwo((int)Math.sqrt(maxGlyphHeight * maxGlyphHeight * charactersLength));
                if (MAX_SIZE > 0) {
                    size = Math.min(size, MAX_SIZE);
                }
                packStrategy = new ScanlinePackStrategy();
            }
            ownsAtlas = true;
            packer = new Packer(Image.Format.RGBA8, size, size, 1, false, packStrategy);
            packer.setTransparentColor(parameter.color);
            packer.getTransparentColor().a = 0;
            if (parameter.borderWidth > 0) {
                packer.setTransparentColor(parameter.borderColor);
                packer.getTransparentColor().a = 0;
            }
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

            int height = face.loadChar(c, flags) ? FtLibrary.toInt(face.getGlyph().getMetrics().getHeight()) : 0;
            heights[i] = height;

            if (c == '\0') {
                BitmapCharacter missingGlyph = createGlyph('\0', data, parameter, stroker, baseLine, packer);
                if (missingGlyph != null && missingGlyph.getWidth() != 0 && missingGlyph.getHeight() != 0) {
                    data.setGlyph('\0', missingGlyph);
                    data.missingGlyph = missingGlyph;
                    if (incremental) data.glyphs.add(missingGlyph);
                }
            }
        }
        int heightsCount = heights.length;
        while (heightsCount > 0) {
            int best = 0, maxHeight = heights[0];
            for (int i = 1; i < heightsCount; i++) {
                int height = heights[i];
                if (height > maxHeight) {
                    maxHeight = height;
                    best = i;
                }
            }

            char c = characters[best];
            if (data.getGlyph(c) == null) {
                BitmapCharacter glyph = createGlyph(c, data, parameter, stroker, baseLine, packer);
                if (glyph != null) {
                    data.setGlyph(c, glyph);
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
                BitmapCharacter first = data.getGlyph(firstChar);
                if (first == null) continue;
                int firstIndex = face.getCharIndex(firstChar);
                for (int ii = i; ii < charactersLength; ii++) {
                    char secondChar = characters[ii];
                    BitmapCharacter second = data.getGlyph(secondChar);
                    if (second == null) continue;
                    int secondIndex = face.getCharIndex(secondChar);

                    long kerning = face.getKerning(firstIndex, secondIndex, FT_KERNING_DEFAULT); // FT_KERNING_DEFAULT (scaled then rounded).
                    if (kerning != 0) first.addKerning(secondChar, FtLibrary.toInt(kerning));

                    kerning = face.getKerning(secondIndex, firstIndex, FT_KERNING_DEFAULT); // FT_KERNING_DEFAULT (scaled then rounded).
                    if (kerning != 0) second.addKerning(firstChar, FtLibrary.toInt(kerning));
                }
            }
        }

        // Generate texture regions.
        if (ownsAtlas) {
            data.regions = new ArrayList<>();
            packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);
        }

        // Set space glyph.
        BitmapCharacter spaceGlyph = data.getGlyph(' ');
        if (spaceGlyph == null) {
            spaceGlyph = new BitmapCharacter();
            spaceGlyph.setXAdvance( (int)data.spaceXadvance + parameter.spaceX );
            spaceGlyph.setChar(' ');
            data.setGlyph(' ', spaceGlyph);
        }
        if (spaceGlyph.getWidth() == 0) {
            spaceGlyph.setWidth( (int)(spaceGlyph.getXAdvance() + data.padRight) );
        }

        return data;
    }

    /** @return null if glyph was not found. */
    protected BitmapCharacter createGlyph(char c, FtBitmapFontData data, FtFontParameter parameter, FtStroker stroker,
                                       float baseLine, Packer packer) {

        boolean missing = face.getCharIndex(c) == 0 && c != 0;
        if (missing) return null;

        if (!face.loadChar(c, parameter.hinting.getLoadFlag())) return null;

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
                for (int i = 0, n = parameter.renderCount; i < n; i++)
                    borderPixmap.drawPixmap(mainPixmap, offsetX, offsetY);

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
                Image shadowPixmap = new Image(mainPixmap.getFormat(), shadowW, shadowH);

                ColorRGBA shadowColor = parameter.shadowColor;
                float a = shadowColor.a;
                if (a != 0) {
                    byte r = (byte)(shadowColor.r * 255), g = (byte)(shadowColor.g * 255), b = (byte)(shadowColor.b * 255);
                    ByteBuffer mainPixels = mainPixmap.getPixels();
                    ByteBuffer shadowPixels = shadowPixmap.getPixels();
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
                for (int i = 0, n = parameter.renderCount; i < n; i++)
                    shadowPixmap.drawPixmap(mainPixmap, Math.max(-parameter.shadowOffsetX, 0), Math.max(-parameter.shadowOffsetY, 0));
                mainPixmap.dispose();
                mainPixmap = shadowPixmap;
            } else if (parameter.borderWidth == 0) {
                // No shadow and no border, draw glyph additional times.
                for (int i = 0, n = parameter.renderCount - 1; i < n; i++)
                    mainPixmap.drawPixmap(mainPixmap, 0, 0);
            }

            if (parameter.padTop > 0 || parameter.padLeft > 0 || parameter.padBottom > 0 || parameter.padRight > 0) {
                Pixmap padPixmap = new Pixmap(mainPixmap.getWidth() + parameter.padLeft + parameter.padRight,
                        mainPixmap.getHeight() + parameter.padTop + parameter.padBottom, mainPixmap.getFormat());
                padPixmap.setBlending(Blending.None);
                padPixmap.drawPixmap(mainPixmap, parameter.padLeft, parameter.padTop);
                mainPixmap.close();
                mainPixmap = padPixmap;
            }
        }

        FtGlyphMetrics metrics = slot.getMetrics();
        BitmapCharacter glyph = new BitmapCharacter(c);
        glyph.width = mainPixmap.getWidth();
        glyph.height = mainPixmap.getHeight();
        glyph.xoffset = mainGlyph.getLeft();
        if (parameter.flip)
            glyph.yoffset = -mainGlyph.getTop() + (int)baseLine;
        else
            glyph.yoffset = -(glyph.height - mainGlyph.getTop()) - (int)baseLine;
        glyph.xadvance = FreeType.toInt(metrics.getHoriAdvance()) + (int)parameter.borderWidth + parameter.spaceX;

        if (bitmapped) {
            mainPixmap.setColor(ColorRGBA.BlackNoAlpha);
            mainPixmap.fill();
            ByteBuffer buf = mainBitmap.getBuffer();
            int whiteIntBits = Color.WHITE.toIntBits();
            int clearIntBits = Color.CLEAR.toIntBits();
            for (int h = 0; h < glyph.height; h++) {
                int idx = h * mainBitmap.getPitch();
                for (int w = 0; w < (glyph.width + glyph.xoffset); w++) {
                    int bit = (buf.get(idx + (w / 8)) >>> (7 - (w % 8))) & 1;
                    mainPixmap.drawPixel(w, h, ((bit == 1) ? whiteIntBits : clearIntBits));
                }
            }
        }

        String pixmapName = glyph.hashCode() + "_" + glyph.getChar();
        Rectangle rect = packer.pack(pixmapName, mainPixmap);
        glyph.page = packer.getPageIndex(pixmapName);
        glyph.x = (int)rect.x;
        glyph.y = (int)rect.y;

        // If a page was added, create a new texture region for the incrementally added glyph.
        if (parameter.incremental && data.regions != null && data.regions.size() <= glyph.getPage())
            packer.updateTextureRegions(data.regions, parameter.minFilter, parameter.magFilter, parameter.genMipMaps);

        mainPixmap.close();
        mainGlyph.close();

        return glyph;
    }
}
