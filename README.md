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

* Load *.ttf fonts and render them with the support of freetype. The backend now is lwjgl-freetype.
* Generate a BitmapFont of your desired size on the fly.
* Seamless integration with the original BitmapFont and BitmapText. This is mainly to allow Lemur to use the new font directly, and other jME3 user projects can also easily replace the font.
* A font editor tool like hiero, in pure jME3 way. User can preview the font, save and load font presets with it. 

Other important features(in plan):

* Emoji 🐒!!
* Support generating SDF fonts and rendering them correctly.
* Support font fallback. Users can set the priority of fonts like CSS style sheets.
* With the support of harfbuzz, correctly handle glyphs and accurately recognize languages with different writing script.

## How to use


### dependency

maven:

```xml
<dependencies>

    <dependency>
        <groupId>io.github.jmecn</groupId>
        <artifactId>jme3-freetype-font</artifactId>
        <version>0.1.2</version>
    </dependency>

    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-freetype</artifactId>
        <version>3.3.2</version>
        <classifier>natives-windows</classifier>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

gradle:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.jmecn:jme3-freetype-font:0.1.2"
    implementation "org.lwjgl:lwjgl-freetype::natives-windows:3.3.2"
}
```

### usage

Use `FtFontKey` and `FtFontLoader`, you can specify many parameters to load a font.

```java
public void simpleInitApp() {
    assetManager.registerLoader(FtFontLoader.class, "ttf", "otf");
    FtFontKey key = new FtFontKey("fonts/NotoSans-Regular.ttf", 16);
    // set the payload characters. It's optional.
    key.setCharacters("abcdefghijklmnopqrstuvwxyz");
    // enable real-time glyph generation.
    key.setIncremental(true);
    BitmapFont font = assetManager.loadFont(key);
    BitmapText text = new BitmapText(font);
}
```

Use `FtBitmapFont` to create a font quickly. All parameters ara default.

```java
public void simpleInitApp() {
    FtBitmapFont font = new FtBitmapFont(assetManager, "fonts/NotoSans-Regular.ttf", 16);
    BitmapText text = new BitmapText(font);
}
```

Use `FtFontGenerater` and `FtFontParameter` to create a font manually.

```java
public void simpleInitApp() {
    FtFontGenerater generater = new FtFontGenerater(new File("fonts/NotoSans-Regular.ttf"));

    FtFontParameter params = new FtFontParameter();
    params.setSize(16);
    params.setIncremental(true);
    params.setMono(true);
    params.setMagFilter(Texture.MagFilter.Nearest);
    params.setMinFilter(Texture.MinFilter.NearestNoMipMap);

    params.setBorderWidth(1);
    params.setBorderColor(ColorRGBA.Black);
    
    params.setShadowOffsetX(3);
    params.setShadowOffsetY(3);
    params.setShadowColor(ColorRGBA.DaryGray);

    FtBitmapCharacterSet charSet = generater.generateData(params);

    BitmapFont font = new BitmapFont();
    font.setCharSet(charSet);
}
```

Use Distance-field font. You need to change the default MatDef to any other SDF font support shader. I copied on from freetype-gl as a default MatDef.

DON'T set BorderWidth or ShadowOffsetX/Y with SDF font.

```java
public void simpleInitApp() {
    assetManager.registerLoader(FtFontLoader.class, "otf");
    FtFontKey key = new FtFontKey("Font/NotoSerifSC-Regular.otf", 64, true);
    key.setRenderMode(RenderMode.SDF);// specify the render mode
    key.setSpread(8);// specify the spread, range in [2, 32]
    key.setMatDefName("Shaders/Font/SdFont.j3md");// specify the Shader
    key.setColorMapParamName("ColorMap");
    key.setUseVertexColor(false);// SdFont currently doesn't support vertex color, so turn it off
    BitmapFont fnt = assetManager.loadAsset(key);
}
```

Enjoy yourself.

## Note

### version

As this library depends on lwjgl-freetype, it's a module of lwjgl3. User should use jme3-lwjgl3 instead of jme3-lwjgl for jme3 application.

For jme3 version 3.6.1-stable, the jme3-lwjgl3 module depends on lwjgl3 3.3.2. So the version of lwjgl-freetype now is 3.3.2 too.

If your jme3-lwjgl3 version is newer or older, you should check the version of lwjgl-freetype, keep it the same to lwjgl3 version.

### runtime natives

lwjgl3 support natives for windows, macos and linux. You can find the natives at https://www.lwjgl.org/customize

maven: 

```xml
<properties>
	<lwjgl.version>3.3.2</lwjgl.version>
</properties>

<profiles>
	<profile>
		<id>lwjgl-natives-linux-amd64</id>
		<activation>
			<os>
				<family>unix</family>
				<name>linux</name>
				<arch>amd64</arch>
			</os>
		</activation>
		<properties>
			<lwjgl.natives>natives-linux</lwjgl.natives>
		</properties>
	</profile>
	<profile>
		<id>lwjgl-natives-macos-x86_64</id>
		<activation>
			<os>
				<family>mac</family>
				<arch>x86_64</arch>
			</os>
		</activation>
		<properties>
			<lwjgl.natives>natives-macos</lwjgl.natives>
		</properties>
	</profile>
	<profile>
		<id>lwjgl-natives-windows-amd64</id>
		<activation>
			<os>
				<family>windows</family>
				<arch>amd64</arch>
			</os>
		</activation>
		<properties>
			<lwjgl.natives>natives-windows</lwjgl.natives>
		</properties>
	</profile>
</profiles>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-bom</artifactId>
            <version>${lwjgl.version}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl</artifactId>
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-freetype</artifactId>
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl</artifactId>
        <classifier>${lwjgl.natives}</classifier>
    </dependency>
    <dependency>
        <groupId>org.lwjgl</groupId>
        <artifactId>lwjgl-freetype</artifactId>
        <classifier>${lwjgl.natives}</classifier>
    </dependency>
</dependencies>
```

gradle:

```groovy
import org.gradle.internal.os.OperatingSystem

project.ext.lwjglVersion = "3.3.2"

switch (OperatingSystem.current()) {
	case OperatingSystem.LINUX:
		project.ext.lwjglNatives = "natives-linux"
		def osArch = System.getProperty("os.arch")
		if (osArch.startsWith("arm") || osArch.startsWith("aarch64")) {
			project.ext.lwjglNatives += osArch.contains("64") || osArch.startsWith("armv8") ? "-arm64" : "-arm32"
		} else if  (osArch.startsWith("ppc")) {
			project.ext.lwjglNatives += "-ppc64le"
		} else if  (osArch.startsWith("riscv")) {
			project.ext.lwjglNatives += "-riscv64"
		}
		break
	case OperatingSystem.MAC_OS:
		project.ext.lwjglNatives = "natives-macos"
		break
	case OperatingSystem.WINDOWS:
		project.ext.lwjglNatives = "natives-windows"
		break
}

repositories {
	mavenCentral()
}

dependencies {
	implementation platform("org.lwjgl:lwjgl-bom:$lwjglVersion")

	implementation "org.lwjgl:lwjgl"
	implementation "org.lwjgl:lwjgl-freetype"
	runtimeOnly "org.lwjgl:lwjgl::$lwjglNatives"
	runtimeOnly "org.lwjgl:lwjgl-freetype::$lwjglNatives"
}
```