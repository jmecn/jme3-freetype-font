package io.github.jmecn.font.generator;

import com.jme3.font.BitmapCharacter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GlyphRun {
	public ArrayList<BitmapCharacter> glyphs = new ArrayList<>();

	/** Contains glyphs.size+1 entries:<br>
	 * The first entry is the X offset relative to the drawing position.<br>
	 * Subsequent entries are the X advance relative to previous glyph position.<br>
	 * The last entry is the width of the last glyph. */
	public ArrayList<Float> xAdvances = new ArrayList<>();

	public float x, y, width;

	void appendRun(GlyphRun run) {
		glyphs.addAll(run.glyphs);
		// Remove the width of the last glyph. The first xadvance of the appended run has kerning for the last glyph of this run.
		if (!xAdvances.isEmpty()) {
			xAdvances.remove(xAdvances.size() - 1);
		}
		xAdvances.addAll(run.xAdvances);
	}

	public void reset () {
		glyphs.clear();
		xAdvances.clear();
	}

	public String toString () {
		StringBuilder buffer = new StringBuilder(glyphs.size() + 32);
		List<BitmapCharacter> glyphs = this.glyphs;
		for (int i = 0, n = glyphs.size(); i < n; i++) {
			BitmapCharacter g = glyphs.get(i);
			buffer.append(g.getChar());
		}
		buffer.append(", ");
		buffer.append(x);
		buffer.append(", ");
		buffer.append(y);
		buffer.append(", ");
		buffer.append(width);
		return buffer.toString();
	}
}