package io.github.jmecn.font.loader;

import com.jme3.font.BitmapCharacter;
import io.github.jmecn.font.exception.FtRuntimeException;
import io.github.jmecn.font.generator.BitmapFontData;
import io.github.jmecn.font.generator.Glyph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class BitmapFontLoader {

    public BitmapFontData load(File fontFile, boolean flip) {
        BitmapFontData data = new BitmapFontData();
        data.fontFile = fontFile;
        data.flipped = flip;
        load(data, fontFile, flip);
        return data;
    }

    public void load(BitmapFontData data, File fontFile, boolean flip) {
        if (data.imagePaths != null) throw new IllegalStateException("Already loaded.");

        data.name = fontFile.getName();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fontFile)), 512)) {
            String line = reader.readLine(); // info
            if (line == null) throw new FtRuntimeException("File is empty.");

            line = line.substring(line.indexOf("padding=") + 8);
            String[] padding = line.substring(0, line.indexOf(' ')).split(",", 4);
            if (padding.length != 4) throw new FtRuntimeException("Invalid padding.");
            data.padTop = Integer.parseInt(padding[0]);
            data.padRight = Integer.parseInt(padding[1]);
            data.padBottom = Integer.parseInt(padding[2]);
            data.padLeft = Integer.parseInt(padding[3]);
            float padY = data.padTop + data.padBottom;

            line = reader.readLine();
            if (line == null) throw new FtRuntimeException("Missing common header.");
            String[] common = line.split(" ", 9); // At most we want the 6th element; i.e. "page=N"

            // At least lineHeight and base are required.
            if (common.length < 3) throw new FtRuntimeException("Invalid common header.");

            if (!common[1].startsWith("lineHeight=")) throw new FtRuntimeException("Missing: lineHeight");
            data.lineHeight = Integer.parseInt(common[1].substring(11));

            if (!common[2].startsWith("base=")) throw new FtRuntimeException("Missing: base");
            float baseLine = Integer.parseInt(common[2].substring(5));

            int pageCount = 1;
            if (common.length >= 6 && common[5] != null && common[5].startsWith("pages=")) {
                try {
                    pageCount = Math.max(1, Integer.parseInt(common[5].substring(6)));
                } catch (NumberFormatException ignored) { // Use one page.
                }
            }

            data.imagePaths = new String[pageCount];

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

                data.imagePaths[p] = (fontFile.getParent() + File.pathSeparator + fileName).replace("\\\\", "/");
            }
            data.descent = 0;

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
                    data.missingGlyph = glyph;
                else if (ch <= Character.MAX_VALUE)
                    data.setGlyph(ch, glyph);
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

                if (glyph.getWidth() > 0 && glyph.getHeight() > 0) {
                    data.descent = Math.min(baseLine + glyph.getYOffset(), data.descent);
                }
            }
            data.descent += data.padBottom;

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
                BitmapCharacter glyph = data.getGlyph((char)first);
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

            Glyph spaceGlyph = data.getGlyph(' ');
            if (spaceGlyph == null) {
                spaceGlyph = new Glyph();
                spaceGlyph.setChar(' ');
                BitmapCharacter xadvanceGlyph = data.getGlyph('l');
                if (xadvanceGlyph == null) xadvanceGlyph = data.getFirstGlyph();
                spaceGlyph.setXAdvance( xadvanceGlyph.getXAdvance() );
                data.setGlyph(' ', spaceGlyph);
            }
            if (spaceGlyph.getWidth() == 0) {
                spaceGlyph.setWidth( (int)(data.padLeft + spaceGlyph.getXAdvance() + data.padRight) );
                spaceGlyph.setXOffset( (int)-data.padLeft );
            }
            data.spaceXadvance = spaceGlyph.getXAdvance();

            BitmapCharacter xGlyph = null;
            for (char xChar : data.xChars) {
                xGlyph = data.getGlyph(xChar);
                if (xGlyph != null) break;
            }
            if (xGlyph == null) xGlyph = data.getFirstGlyph();
            data.xHeight = xGlyph.getHeight() - padY;

            BitmapCharacter capGlyph = null;
            for (char capChar : data.capChars) {
                capGlyph = data.getGlyph(capChar);
                if (capGlyph != null) break;
            }
            if (capGlyph == null) {
                for (BitmapCharacter[] page : data.glyphs) {
                    if (page == null) continue;
                    for (BitmapCharacter glyph : page) {
                        if (glyph == null || glyph.getHeight() == 0 || glyph.getWidth() == 0) continue;
                        data.capHeight = Math.max(data.capHeight, glyph.getHeight());
                    }
                }
            } else
                data.capHeight = capGlyph.getHeight();
            data.capHeight -= padY;

            data.ascent = baseLine - data.capHeight;
            data.down = -data.lineHeight;
            if (flip) {
                data.ascent = -data.ascent;
                data.down = -data.down;
            }

            if (hasMetricsOverride) {
                data.ascent = overrideAscent;
                data.descent = overrideDescent;
                data.down = overrideDown;
                data.capHeight = overrideCapHeight;
                data.lineHeight = overrideLineHeight;
                data.spaceXadvance = overrideSpaceXAdvance;
                data.xHeight = overrideXHeight;
            }

        } catch (Exception ex) {
            throw new FtRuntimeException("Error loading font file: " + fontFile, ex);
        }
    }
}
