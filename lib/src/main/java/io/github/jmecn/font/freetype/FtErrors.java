package io.github.jmecn.font.freetype;

import io.github.jmecn.font.exception.FtRuntimeException;

import static org.lwjgl.util.freetype.FreeType.FT_Err_Max;
import static org.lwjgl.util.freetype.FreeType.FT_Err_Ok;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtErrors {

    static final String[] errors;
    static {
        errors = new String[FT_Err_Max];

        /* generic errors */

        errors[0x01] = "cannot open resource";
        errors[0x02] = "unknown file format";
        errors[0x03] = "broken file";
        errors[0x04] = "invalid FreeType version";
        errors[0x05] = "module version is too low";
        errors[0x06] = "invalid argument";
        errors[0x07] = "unimplemented feature";
        errors[0x08] = "broken table";
        errors[0x09] = "broken offset within table";
        errors[0x0A] = "array allocation size too large";
        errors[0x0B] = "missing module";
        errors[0x0C] = "missing property";

        /* glyph/character errors */

        errors[0x10] = "invalid glyph index";
        errors[0x11] = "invalid character code";
        errors[0x12] = "unsupported glyph image format";
        errors[0x13] = "cannot render this glyph format";
        errors[0x14] = "invalid outline";
        errors[0x15] = "invalid composite glyph";
        errors[0x16] = "too many hints";
        errors[0x17] = "invalid pixel size";
        errors[0x18] = "invalid SVG document";

        /* handle errors */

        errors[0x20] = "invalid object handle";
        errors[0x21] = "invalid library handle";
        errors[0x22] = "invalid module handle";
        errors[0x23] = "invalid face handle";
        errors[0x24] = "invalid size handle";
        errors[0x25] = "invalid glyph slot handle";
        errors[0x26] = "invalid charmap handle";
        errors[0x27] = "invalid cache manager handle";
        errors[0x28] = "invalid stream handle";

        /* driver errors */

        errors[0x30] = "too many modules";
        errors[0x31] = "too many extensions";

        /* memory errors */

        errors[0x40] = "out of memory";
        errors[0x41] = "unlisted object";

        /* stream errors */

        errors[0x51] = "cannot open stream";
        errors[0x52] = "invalid stream seek";
        errors[0x53] = "invalid stream skip";
        errors[0x54] = "invalid stream read";
        errors[0x55] = "invalid stream operation";
        errors[0x56] = "invalid frame operation";
        errors[0x57] = "nested frame access";
        errors[0x58] = "invalid frame read";

        /* raster errors */

        errors[0x60] = "raster uninitialized";
        errors[0x61] = "raster corrupted";
        errors[0x62] = "raster overflow";
        errors[0x63] = "negative height while rastering";

        /* cache errors */

        errors[0x70] = "too many registered caches";

        /* TrueType and SFNT errors */

        errors[0x80] = "invalid opcode";
        errors[0x81] = "too few arguments";
        errors[0x82] = "stack overflow";
        errors[0x83] = "code overflow";
        errors[0x84] = "bad argument";
        errors[0x85] = "division by zero";
        errors[0x86] = "invalid reference";
        errors[0x87] = "found debug opcode";
        errors[0x88] = "found ENDF opcode in execution stream";
        errors[0x89] = "nested DEFS";
        errors[0x8A] = "invalid code range";
        errors[0x8B] = "execution context too long";
        errors[0x8C] = "too many function definitions";
        errors[0x8D] = "too many instruction definitions";
        errors[0x8E] = "SFNT font table missing";
        errors[0x8F] = "horizontal header (hhea) table missing";
        errors[0x90] = "locations (loca) table missing";
        errors[0x91] = "name table missing";
        errors[0x92] = "character map (cmap) table missing";
        errors[0x93] = "horizontal metrics (hmtx) table missing";
        errors[0x94] = "PostScript (post) table missing";
        errors[0x95] = "invalid horizontal metrics";
        errors[0x96] = "invalid character map (cmap) format";
        errors[0x97] = "invalid ppem value";
        errors[0x98] = "invalid vertical metrics";
        errors[0x99] = "could not find context";
        errors[0x9A] = "invalid PostScript (post) table format";
        errors[0x9B] = "invalid PostScript (post) table";
        errors[0x9C] = "found FDEF or IDEF opcode in glyf bytecode";
        errors[0x9D] = "missing bitmap in strike";
        errors[0x9E] = "SVG hooks have not been set";

        /* CFF, CID, and Type 1 errors */

        errors[0xA0] = "opcode syntax error";
        errors[0xA1] = "argument stack underflow";
        errors[0xA2] = "ignore";
        errors[0xA3] = "no Unicode glyph name found";
        errors[0xA4] = "glyph too big for hinting";

        /* BDF errors */

        errors[0xB0] = "`STARTFONT' field missing";
        errors[0xB1] = "`FONT' field missing";
        errors[0xB2] = "`SIZE' field missing";
        errors[0xB3] = "`FONTBOUNDINGBOX' field missing";
        errors[0xB4] = "`CHARS' field missing";
        errors[0xB5] = "`STARTCHAR' field missing";
        errors[0xB6] = "`ENCODING' field missing";
        errors[0xB7] = "`BBX' field missing";
        errors[0xB8] = "`BBX' too big";
        errors[0xB9] = "Font header corrupted or missing fields";
        errors[0xBA] = "Font glyphs corrupted or missing fields";
    }

    public static void ok(int err) {
        if (err == FT_Err_Ok) {
            return;
        }

        if (err < 0 || err > FT_Err_Max) {
            throw new FtRuntimeException("Unknown error:" + err);
        }

        String msg = errors[err];
        if (msg == null) {
            throw new FtRuntimeException("Unknown freetype error:" + err);
        } else {
            throw new FtRuntimeException(msg);
        }
    }
}
