package io.github.jmecn.font;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

/**
 * desc: FreeType font
 *
 * @author yanmaoyuan
 */
public class FtBitmapText extends BitmapText {

    public FtBitmapText(BitmapFont font) {
        super(font);
    }

    public FtBitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased) {
        super(font, rightToLeft, arrayBased);
    }
}
