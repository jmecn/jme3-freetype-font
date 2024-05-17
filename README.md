# jme3-freetype-font

- lib: The jme3-freetype-font library source code
- font: Fonts used for testing and demo

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

Core (in plan):

* Load *.ttf fonts and render them with the support of freetype 1. The backend now is lwjgl-freetype.
* Generate a BitmapFont of your desired size on the fly.
* Seamless integration with the original BitmapFont and BitmapText. This is mainly to allow Lemur to use the new font directly, and other jME3 user projects can also easily replace the font.
* A font editor tool like hiero, in pure jME3 way. User can preview the font, save and load font presets with it. 

Other important features(in plan):

* Emoji 🐒!!
* Support generating SDF fonts and rendering them correctly.
* Support font fallback. Users can set the priority of fonts like CSS style sheets.
* With the support of harfbuzz, correctly handle glyphs and accurately recognize languages with different writing script.

## How to use

