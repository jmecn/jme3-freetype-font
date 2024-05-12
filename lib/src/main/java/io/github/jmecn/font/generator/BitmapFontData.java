package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;
import com.jme3.texture.Image;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.packer.TextureRegion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class BitmapFontData {
    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
    private static final int PAGES = 0x10000 / PAGE_SIZE;

    /** The name of the font, or null. */
    public String name;
    /** An array of the image paths, for multiple texture pages. */
    public String[] imagePaths;
    public File fontFile;
    public boolean flipped;
    public float padTop, padRight, padBottom, padLeft;
    /** The distance from one line of text to the next. To set this value, use {@link #setLineHeight(float)}. */
    public float lineHeight;
    /** The distance from the top of most uppercase characters to the baseline. Since the drawing position is the cap height of
     * the first line, the cap height can be used to get the location of the baseline. */
    public float capHeight = 1;
    /** The distance from the cap height to the top of the tallest glyph. */
    public float ascent;
    /** The distance from the bottom of the glyph that extends the lowest to the baseline. This number is negative. */
    public float descent;
    /** The distance to move down when \n is encountered. */
    public float down;
    /** Multiplier for the line height of blank lines. down * blankLineHeight is used as the distance to move down for a blank
     * line. */
    public float blankLineScale = 1;
    public float scaleX = 1, scaleY = 1;
    public boolean markupEnabled;
    /** The amount to add to the glyph X position when drawing a cursor between glyphs. This field is not set by the BMFont
     * file, it needs to be set manually depending on how the glyphs are rendered on the backing textures. */
    public float cursorX;

    public final Glyph[][] glyphs = new Glyph[PAGES][];
    /** The glyph to display for characters not in the font. May be null. */
    public Glyph missingGlyph;

    /** The width of the space character. */
    public float spaceXadvance;
    /** The x-height, which is the distance from the top of most lowercase characters to the baseline. */
    public float xHeight = 1;

    /** Additional characters besides whitespace where text is wrapped. Eg, a hypen (-). */
    public char[] breakChars;
    public char[] xChars = {'x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z'};
    public char[] capChars = {'M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A', 'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public BitmapFontData () {
    }

    public BitmapFontData (File fontFile, boolean flip) {
        this.fontFile = fontFile;
        this.flipped = flip;
        load(fontFile, flip);
    }

    public void load (File fontFile, boolean flip) {
        if (imagePaths != null) throw new IllegalStateException("Already loaded.");

        name = fontFile.getName();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fontFile)), 512)) {
            String line = reader.readLine(); // info
            if (line == null) throw new FtRuntimeException("File is empty.");

            line = line.substring(line.indexOf("padding=") + 8);
            String[] padding = line.substring(0, line.indexOf(' ')).split(",", 4);
            if (padding.length != 4) throw new FtRuntimeException("Invalid padding.");
            padTop = Integer.parseInt(padding[0]);
            padRight = Integer.parseInt(padding[1]);
            padBottom = Integer.parseInt(padding[2]);
            padLeft = Integer.parseInt(padding[3]);
            float padY = padTop + padBottom;

            line = reader.readLine();
            if (line == null) throw new FtRuntimeException("Missing common header.");
            String[] common = line.split(" ", 9); // At most we want the 6th element; i.e. "page=N"

            // At least lineHeight and base are required.
            if (common.length < 3) throw new FtRuntimeException("Invalid common header.");

            if (!common[1].startsWith("lineHeight=")) throw new FtRuntimeException("Missing: lineHeight");
            lineHeight = Integer.parseInt(common[1].substring(11));

            if (!common[2].startsWith("base=")) throw new FtRuntimeException("Missing: base");
            float baseLine = Integer.parseInt(common[2].substring(5));

            int pageCount = 1;
            if (common.length >= 6 && common[5] != null && common[5].startsWith("pages=")) {
                try {
                    pageCount = Math.max(1, Integer.parseInt(common[5].substring(6)));
                } catch (NumberFormatException ignored) { // Use one page.
                }
            }

            imagePaths = new String[pageCount];

            // Read each page definition.
            for (int p = 0; p < pageCount; p++) {
                // Read each "page" info line.
                line = reader.readLine();
                if (line == null) throw new FtRuntimeException("Missing additional page definitions.");

                // Expect ID to mean "index".
                Matcher matcher = Pattern.compile(".*id=(\\d+)").matcher(line);
                if (matcher.find()) {
                    String id = matcher.group(1);
                    try {
                        int pageID = Integer.parseInt(id);
                        if (pageID != p) throw new FtRuntimeException("Page IDs must be indices starting at 0: " + id);
                    } catch (NumberFormatException ex) {
                        throw new FtRuntimeException("Invalid page id: " + id, ex);
                    }
                }

                matcher = Pattern.compile(".*file=\"?([^\"]+)\"?").matcher(line);
                if (!matcher.find()) throw new FtRuntimeException("Missing: file");
                String fileName = matcher.group(1);

                imagePaths[p] = (fontFile.getParent() + File.pathSeparator + fileName).replace("\\\\", "/");
            }
            descent = 0;

            while (true) {
                line = reader.readLine();
                if (line == null) break; // EOF
                if (line.startsWith("kernings ")) break; // Starting kernings block.
                if (line.startsWith("metrics ")) break; // Starting metrics block.
                if (!line.startsWith("char ")) continue;

                Glyph glyph = new Glyph();

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                int ch = Integer.parseInt(tokens.nextToken());
                if (ch <= 0)
                    missingGlyph = glyph;
                else if (ch <= Character.MAX_VALUE)
                    setGlyph(ch, glyph);
                else
                    continue;
                glyph.setChar((char) ch);
                tokens.nextToken();
                glyph.setX( Integer.parseInt(tokens.nextToken()) );
                tokens.nextToken();
                glyph.setY( Integer.parseInt(tokens.nextToken()) );
                tokens.nextToken();
                glyph.setWidth( Integer.parseInt(tokens.nextToken()) );
                tokens.nextToken();
                glyph.setHeight( Integer.parseInt(tokens.nextToken()) );
                tokens.nextToken();
                glyph.setXOffset( Integer.parseInt(tokens.nextToken()) );
                tokens.nextToken();
                if (flip)
                    glyph.setYOffset( Integer.parseInt(tokens.nextToken()) );
                else
                    glyph.setYOffset( -(glyph.getHeight() + Integer.parseInt(tokens.nextToken())) );
                tokens.nextToken();
                glyph.setXAdvance( Integer.parseInt(tokens.nextToken()) );

                // Check for page safely, it could be omitted or invalid.
                if (tokens.hasMoreTokens())  {
                    tokens.nextToken();
                }
                if (tokens.hasMoreTokens()) {
                    try {
                        glyph.setPage( Integer.parseInt(tokens.nextToken()) );
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (glyph.getWidth() > 0 && glyph.getHeight() > 0) descent = Math.min(baseLine + glyph.getYOffset(), descent);
            }
            descent += padBottom;

            while (true) {
                line = reader.readLine();
                if (line == null) break;
                if (!line.startsWith("kerning ")) break;

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                int first = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int second = Integer.parseInt(tokens.nextToken());
                if (first < 0 || first > Character.MAX_VALUE || second < 0 || second > Character.MAX_VALUE) continue;
                BitmapCharacter glyph = getGlyph((char)first);
                tokens.nextToken();
                int amount = Integer.parseInt(tokens.nextToken());
                if (glyph != null) { // Kernings may exist for glyph pairs not contained in the font.
                    glyph.addKerning(second, amount);
                }
            }

            boolean hasMetricsOverride = false;
            float overrideAscent = 0;
            float overrideDescent = 0;
            float overrideDown = 0;
            float overrideCapHeight = 0;
            float overrideLineHeight = 0;
            float overrideSpaceXAdvance = 0;
            float overrideXHeight = 0;

            // Metrics override
            if (line != null && line.startsWith("metrics ")) {

                hasMetricsOverride = true;

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                overrideAscent = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideDescent = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideDown = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideCapHeight = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideLineHeight = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideSpaceXAdvance = Float.parseFloat(tokens.nextToken());
                tokens.nextToken();
                overrideXHeight = Float.parseFloat(tokens.nextToken());
            }

            Glyph spaceGlyph = getGlyph(' ');
            if (spaceGlyph == null) {
                spaceGlyph = new Glyph();
                spaceGlyph.setChar(' ');
                BitmapCharacter xadvanceGlyph = getGlyph('l');
                if (xadvanceGlyph == null) xadvanceGlyph = getFirstGlyph();
                spaceGlyph.setXAdvance( xadvanceGlyph.getXAdvance() );
                setGlyph(' ', spaceGlyph);
            }
            if (spaceGlyph.getWidth() == 0) {
                spaceGlyph.setWidth( (int)(padLeft + spaceGlyph.getXAdvance() + padRight) );
                spaceGlyph.setXOffset( (int)-padLeft );
            }
            spaceXadvance = spaceGlyph.getXAdvance();

            BitmapCharacter xGlyph = null;
            for (char xChar : xChars) {
                xGlyph = getGlyph(xChar);
                if (xGlyph != null) break;
            }
            if (xGlyph == null) xGlyph = getFirstGlyph();
            xHeight = xGlyph.getHeight() - padY;

            BitmapCharacter capGlyph = null;
            for (char capChar : capChars) {
                capGlyph = getGlyph(capChar);
                if (capGlyph != null) break;
            }
            if (capGlyph == null) {
                for (BitmapCharacter[] page : this.glyphs) {
                    if (page == null) continue;
                    for (BitmapCharacter glyph : page) {
                        if (glyph == null || glyph.getHeight() == 0 || glyph.getWidth() == 0) continue;
                        capHeight = Math.max(capHeight, glyph.getHeight());
                    }
                }
            } else
                capHeight = capGlyph.getHeight();
            capHeight -= padY;

            ascent = baseLine - capHeight;
            down = -lineHeight;
            if (flip) {
                ascent = -ascent;
                down = -down;
            }

            if (hasMetricsOverride) {
                this.ascent = overrideAscent;
                this.descent = overrideDescent;
                this.down = overrideDown;
                this.capHeight = overrideCapHeight;
                this.lineHeight = overrideLineHeight;
                this.spaceXadvance = overrideSpaceXAdvance;
                this.xHeight = overrideXHeight;
            }

        } catch (Exception ex) {
            throw new FtRuntimeException("Error loading font file: " + fontFile, ex);
        }
    }

    public void setGlyphRegion (Glyph glyph, TextureRegion region) {
        Image texture = region.getTexture();
        float invTexWidth = 1.0f / texture.getWidth();
        float invTexHeight = 1.0f / texture.getHeight();

        int offsetX = 0, offsetY = 0;
        float u = region.u;
        float v = region.v;
        int regionWidth = region.getRegionWidth();
        int regionHeight = region.getRegionHeight();
//        if (region instanceof AtlasRegion) {
//            // Compensate for whitespace stripped from left and top edges.
//            AtlasRegion atlasRegion = (AtlasRegion)region;
//            offsetX = atlasRegion.offsetX;
//            offsetY = atlasRegion.originalHeight - atlasRegion.packedHeight - atlasRegion.offsetY;
//        }

        int x = glyph.getX();
        int x2 = glyph.getX() + glyph.getWidth();
        int y = glyph.getY();
        int y2 = glyph.getY() + glyph.getHeight();

        // Shift glyph for left and top edge stripped whitespace. Clip glyph for right and bottom edge stripped whitespace.
        // Note if the font region has padding, whitespace stripping must not be used.
        if (offsetX > 0) {
            x -= offsetX;
            if (x < 0) {
                glyph.setWidth(glyph.getWidth() + x);
                glyph.setXOffset(glyph.getXOffset() - x);
                x = 0;
            }
            x2 -= offsetX;
            if (x2 > regionWidth) {
                glyph.setWidth( glyph.getWidth() -(x2 - regionWidth) );
                x2 = regionWidth;
            }
        }
        if (offsetY > 0) {
            y -= offsetY;
            if (y < 0) {
                glyph.setHeight(glyph.getHeight() + y);
                if (glyph.getHeight() < 0) glyph.setHeight(0);
                y = 0;
            }
            y2 -= offsetY;
            if (y2 > regionHeight) {
                int amount = y2 - regionHeight;
                glyph.setHeight(glyph.getHeight() - amount);
                glyph.setYOffset(glyph.getYOffset() + amount);
                y2 = regionHeight;
            }
        }

        // FIXME don't need to calculate it here.
        glyph.u = u + x * invTexWidth;
        glyph.u2 = u + x2 * invTexWidth;
        if (flipped) {
            glyph.v = v + y * invTexHeight;
            glyph.v2 = v + y2 * invTexHeight;
        } else {
            glyph.v2 = v + y * invTexHeight;
            glyph.v = v + y2 * invTexHeight;
        }
    }

    /** Sets the line height, which is the distance from one line of text to the next. */
    public void setLineHeight (float height) {
        lineHeight = height * scaleY;
        down = flipped ? lineHeight : -lineHeight;
    }

    public void setGlyph (int ch, Glyph glyph) {
        Glyph[] page = glyphs[ch / PAGE_SIZE];
        if (page == null) glyphs[ch / PAGE_SIZE] = page = new Glyph[PAGE_SIZE];
        page[ch & PAGE_SIZE - 1] = glyph;
    }

    public Glyph getFirstGlyph () {
        for (Glyph[] page : this.glyphs) {
            if (page == null) continue;
            for (Glyph glyph : page) {
                if (glyph == null || glyph.getHeight() == 0 || glyph.getWidth() == 0) continue;
                return glyph;
            }
        }
        throw new FtRuntimeException("No glyphs found.");
    }

    /** Returns true if the font has the glyph, or if the font has a {@link #missingGlyph}. */
    public boolean hasGlyph (char ch) {
        if (missingGlyph != null) return true;
        return getGlyph(ch) != null;
    }

    /** Returns the glyph for the specified character, or null if no such glyph exists. Note that
     * {@link #getGlyphs(GlyphRun, CharSequence, int, int, BitmapCharacter)} should be be used to shape a string of characters into a list
     * of glyphs. */
    public Glyph getGlyph (char ch) {
        Glyph[] page = glyphs[ch / PAGE_SIZE];
        if (page != null) return page[ch & PAGE_SIZE - 1];
        return null;
    }

    /** Using the specified string, populates the glyphs and positions of the specified glyph run.
     * @param str Characters to convert to glyphs. Will not contain newline or color tags. May contain "[[" for an escaped left
     *           square bracket.
     * @param lastGlyph The glyph immediately before this run, or null if this is run is the first on a line of text. Used tp
     *           apply kerning between the specified glyph and the first glyph in this run. */
    public void getGlyphs (GlyphRun run, CharSequence str, int start, int end, Glyph lastGlyph) {
        int max = end - start;
        if (max == 0) return;
        boolean markupEnabled = this.markupEnabled;
        float scaleX = this.scaleX;
        ArrayList<BitmapCharacter> glyphs = run.glyphs;
        ArrayList<Float> xAdvances = run.xAdvances;

        // Guess at number of glyphs needed.
        glyphs.ensureCapacity(max);
        run.xAdvances.ensureCapacity(max + 1);

        do {
            char ch = str.charAt(start++);
            if (ch == '\r') continue; // Ignore.
            Glyph glyph = getGlyph(ch);
            if (glyph == null) {
                if (missingGlyph == null) continue;
                glyph = missingGlyph;
            }
            glyphs.add(glyph);
            xAdvances.add(lastGlyph == null // First glyph on line, adjust the position so it isn't drawn left of 0.
                    ? (glyph.isFixedWidth() ? 0 : -glyph.getXOffset() * scaleX - padLeft)
                    : (lastGlyph.getXAdvance() + lastGlyph.getKerning(ch)) * scaleX);
            lastGlyph = glyph;

            // "[[" is an escaped left square bracket, skip second character.
            if (markupEnabled && ch == '[' && start < end && str.charAt(start) == '[') start++;
        } while (start < end);
        if (lastGlyph != null) {
            float lastGlyphWidth = lastGlyph.isFixedWidth() ? lastGlyph.getXAdvance() * scaleX
                    : (lastGlyph.getWidth() + lastGlyph.getXOffset()) * scaleX - padRight;
            xAdvances.add(lastGlyphWidth);
        }
    }

    /** Returns the first valid glyph index to use to wrap to the next line, starting at the specified start index and
     * (typically) moving toward the beginning of the glyphs array. */
    public int getWrapIndex (List<BitmapCharacter> glyphs, int start) {
        int i = start - 1;
        Object[] glyphsItems = glyphs.toArray();
        char ch = ((BitmapCharacter)glyphsItems[i]).getChar();
        if (isWhitespace(ch)) return i;
        if (isBreakChar(ch)) i--;
        for (; i > 0; i--) {
            ch = ((BitmapCharacter)glyphsItems[i]).getChar();
            if (isWhitespace(ch) || isBreakChar(ch)) return i + 1;
        }
        return 0;
    }

    public boolean isBreakChar (char c) {
        if (breakChars == null) return false;
        for (char br : breakChars)
            if (c == br) return true;
        return false;
    }

    public boolean isWhitespace (char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    /** Returns the image path for the texture page at the given index (the "id" in the BMFont file). */
    public String getImagePath (int index) {
        return imagePaths[index];
    }

    public String[] getImagePaths () {
        return imagePaths;
    }

    public File getFontFile () {
        return fontFile;
    }

    /** Scales the font by the specified amounts on both axes
     * <p>
     * Note that smoother scaling can be achieved if the texture backing the BitmapFont is using {@link com.jme3.texture.Texture.MagFilter#Bilinear}.
     * The default is Nearest, so use a BitmapFont constructor that takes a {@link TextureRegion}.
     * @throws IllegalArgumentException if scaleX or scaleY is zero. */
    public void setScale (float scaleX, float scaleY) {
        if (scaleX == 0) throw new IllegalArgumentException("scaleX cannot be 0.");
        if (scaleY == 0) throw new IllegalArgumentException("scaleY cannot be 0.");
        float x = scaleX / this.scaleX;
        float y = scaleY / this.scaleY;
        lineHeight *= y;
        spaceXadvance *= x;
        xHeight *= y;
        capHeight *= y;
        ascent *= y;
        descent *= y;
        down *= y;
        padLeft *= x;
        padRight *= x;
        padTop *= y;
        padBottom *= y;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /** Scales the font by the specified amount in both directions.
     * @see #setScale(float, float)
     * @throws IllegalArgumentException if scaleX or scaleY is zero. */
    public void setScale (float scaleXY) {
        setScale(scaleXY, scaleXY);
    }

    /** Sets the font's scale relative to the current scale.
     * @see #setScale(float, float)
     * @throws IllegalArgumentException if the resulting scale is zero. */
    public void scale (float amount) {
        setScale(scaleX + amount, scaleY + amount);
    }

    public String toString () {
        return name != null ? name : super.toString();
    }
}
