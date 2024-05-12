# jme3-freetype-font

- lib: The jme3-freetype-font library source code
- font: Fonts used for testing and demo
- demo: Demo application for different use case.

## framework

| Library    | Usage                |
|:-----------|:---------------------|
| FreeType   | font raster library  |
| Harfbuzz   | text shaping library |
| Fontconfig | find best match font |

## algorithms

| Algorithm             | Usage                                                           |
|:----------------------|:----------------------------------------------------------------|
| rectangle bin packing | how to pack small font bitmap into large texture atlas          |
| SDF | signed distance field font generation and shading               |
| MSDF | multi-channel signed distance field font generation and shading |

* https://github.com/juj/RectangleBinPack

## feature

- Load TrueType, OpenType font with freetype.
- Generate BitmapFont on the fly.
- Support emoji.

## RenderMode

| MODE                  | Bit    | Description                                                                                                                                                                                                                                                                                                                                                                                                                    |
|:----------------------|:-------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FT_RENDER_MODE_NORMAL | 8 * 1  | Default render mode; it corresponds to 8-bit anti-aliased bitmaps.                                                                                                                                                                                                                                                                                                                                                             |
| FT_RENDER_MODE_LIGHT  | 8 * 1  | This is equivalent to FT_RENDER_MODE_NORMAL. It is only defined as a separate value because render modes are also used indirectly to define hinting algorithm selectors. See FT_LOAD_TARGET_XXX for details.                                                                                                                                                                                                                   |
| FT_RENDER_MODE_MONO   | 1 * 1  | This mode corresponds to 1-bit bitmaps (with 2 levels of opacity).                                                                                                                                                                                                                                                                                                                                                             |
| FT_RENDER_MODE_LCD    | 24 * 1 | This mode corresponds to horizontal RGB and BGR subpixel displays like LCD screens. It produces 8-bit bitmaps that are 3 times the width of the original glyph outline in pixels, and which use the FT_PIXEL_MODE_LCD mode.                                                                                                                                                                                                    |
| FT_RENDER_MODE_LCD_V  | 8 * 3  | This mode corresponds to vertical RGB and BGR subpixel displays (like PDA screens, rotated LCD displays, etc.). It produces 8-bit bitmaps that are 3 times the height of the original glyph outline in pixels and use the FT_PIXEL_MODE_LCD_V mode.                                                                                                                                                                            |
| FT_RENDER_MODE_SDF    | 8 * 1  | This mode corresponds to 8-bit, single-channel signed distance field (SDF) bitmaps. Each pixel in the SDF grid is the value from the pixel's position to the nearest glyph's outline. The distances are calculated from the center of the pixel and are positive if they are filled by the outline (i.e., inside the outline) and negative otherwise. Check the note below on how to convert the output values to usable data. |

## KerningMode