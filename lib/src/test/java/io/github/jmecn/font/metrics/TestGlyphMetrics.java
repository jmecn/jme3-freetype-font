package io.github.jmecn.font.metrics;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import io.github.jmecn.font.freetype.*;
import io.github.jmecn.font.utils.DebugPrintUtils;
import io.github.jmecn.font.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.util.freetype.FreeType.*;

/**
 * This application use to visualize and compare the glyph metrics. show both vertical and horizontal metrics.
 *
 * @author yanmaoyuan
 */
public class TestGlyphMetrics extends SimpleApplication {

    static Logger logger = LoggerFactory.getLogger(TestGlyphMetrics.class);

    private static final int loadFlags = FT_LOAD_DEFAULT | FT_LOAD_NO_BITMAP;

    private final TestGlyph[] params;
    // private static final float[] SCALES = {0.25f, 0.5f, 1, 2};
    private static final float[] SCALES = {1};

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(800, 600);
        settings.setSamples(4);

        TestGlyph[] params = {
                new TestGlyph("font/NotoSerifSC-Regular.otf", 64, '东', FT_RENDER_MODE_SDF).setGamma(1f),
                new TestGlyph("font/NotoSerifSC-Regular.otf", 64, '东', FT_RENDER_MODE_NORMAL),
                new TestGlyph("font/NotoSerifSC-Regular.otf", 64, '东', FT_RENDER_MODE_MONO).setMagFilter(Texture.MagFilter.Nearest),
        };
        TestGlyphMetrics app = new TestGlyphMetrics(params);
        app.setSettings(settings);
        app.start();
    }

    public TestGlyphMetrics(TestGlyph... params) {
        super();
        this.params = params;
    }

    private void loadMetrics() {
        Map<String, FtFace> faceMap = new HashMap<>();

        try(FtLibrary library = new FtLibrary()) {
            for (TestGlyph param : params) {
                FtFace face = faceMap.computeIfAbsent(param.getFont(), (k) -> library.newFace(param.getFont()));
                if (param.getRenderMode() == FT_RENDER_MODE_SDF) {
                    library.setSdfSpread(param.getSpread());
                }
                loadGlyphMetrics(face, param);
            }
            // close face
            faceMap.values().forEach(FtFace::close);
        }
    }

    private void loadGlyphMetrics(FtFace face, TestGlyph glyph) {
        face.setPixelSize(0, glyph.getPixelSize());

        if (!face.loadChar(glyph.getCh(), loadFlags)) {
            logger.warn("load glyph failed, char:{}", glyph.getCh());
            return;
        }

        FtGlyphSlot slot = face.getGlyph();

        // read face metrics
        FtSizeMetrics sizeMetrics = face.getSize().getMetrics();

        glyph.setLineHeight(FtLibrary.from26D6(sizeMetrics.getHeight()));
        glyph.setMaxAdvance(FtLibrary.from26D6(sizeMetrics.getMaxAdvance()));
        glyph.setAscent(FtLibrary.from26D6(sizeMetrics.getAscender()));
        glyph.setDescent(FtLibrary.from26D6(sizeMetrics.getDescender()));
        glyph.setPpemX(sizeMetrics.getXPpem());
        glyph.setPpemY(sizeMetrics.getYPpem());
        glyph.setScaleX(FtLibrary.from16D16f(sizeMetrics.getXScale()));
        glyph.setScaleY(FtLibrary.from16D16f(sizeMetrics.getYScale()));

        // read glyph metrics
        FtGlyphMetrics metrics = slot.getMetrics();
        glyph.setWidth(FtLibrary.from26D6(metrics.getWidth()));
        glyph.setHeight(FtLibrary.from26D6(metrics.getHeight()));
        glyph.setHoriBearingX(FtLibrary.from26D6(metrics.getHoriBearingX()));
        glyph.setHoriBearingY(FtLibrary.from26D6(metrics.getHoriBearingY()));
        glyph.setHoriAdvance(FtLibrary.from26D6(metrics.getHoriAdvance()));
        glyph.setVertBearingX(FtLibrary.from26D6(metrics.getVertBearingX()));
        glyph.setVertBearingY(FtLibrary.from26D6(metrics.getVertBearingY()));
        glyph.setVertAdvance(FtLibrary.from26D6(metrics.getVertAdvance()));

        // render glyph
        face.renderGlyph(glyph.getRenderMode());
        FtBitmap bitmap = slot.getBitmap();
        if (bitmap.getBufferSize() == 0) {
            logger.warn("no bitmap found");
        } else {
            DebugPrintUtils.printBitmapInfo(bitmap);
            Image image = ImageUtils.ftBitmapToImage(bitmap, glyph.getColor(), glyph.getGamma());
            glyph.setImage(image);
        }

        logger.info("font metrics, lineHeight={}, maxAdvance={}, ascent={}, descent={}, xScale={}, yScale={}, xppem={}, yppem={}",
                glyph.getLineHeight(), glyph.getBase(), glyph.getAscent(), glyph.getDescent(), glyph.getScaleX(), glyph.getScaleY(), glyph.getPpemX(), glyph.getPpemY());
        logger.info("glyph metrics, width={}, height={}, horiBearingX={}, horiBearingY={}, horiAdvance={}, vertBearingX={}, vertBearingY={}, vertAdvance={}",
                glyph.getWidth(), glyph.getHeight(), glyph.getHoriBearingX(), glyph.getHoriBearingY(), glyph.getHoriAdvance(), glyph.getVertBearingX(), glyph.getVertBearingY(), glyph.getVertAdvance());
    }

    private void loadImages() {
        Node node = new Node();
        node.setQueueBucket(RenderQueue.Bucket.Transparent);
        int i = 0;
        float maxScale = SCALES[SCALES.length - 1];
        for (TestGlyph glyph : params) {
            Geometry geom = loadImage(glyph);
            if (geom != null) {
                for (float scale : SCALES) {
                    Geometry copy = geom.clone();
                    copy.scale(scale);
                    copy.move(i * 64, 0, i);
                    node.attachChild(copy);
                }
            }
            i++;
        }
        node.scale(1f / 64);
        rootNode.attachChild(node);
    }
    private Geometry loadImage(TestGlyph glyph) {
        if (glyph.getImage() == null) {
            return null;
        }

        Texture2D texture = new Texture2D(glyph.getImage());
        texture.setMinFilter(Texture2D.MinFilter.NearestNoMipMaps);
        texture.setMagFilter(Texture2D.MagFilter.Bilinear);

        Material material;
        if (glyph.getRenderMode() == FT_RENDER_MODE_SDF) {
            material = new Material(assetManager, "Shaders/Font/SdFont.j3md");
            material.setBoolean("SdfUseAlpha", true);
        } else {
            material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        }
        material.setTexture("ColorMap", texture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        // make all characters the same height
        int imageWidth = glyph.getImage().getWidth();
        int imageHeight = glyph.getImage().getHeight();
        int glyphWidth = glyph.getWidth();
        int glyphHeight = glyph.getHeight();
        int spread = glyph.getSpread();

        Geometry picture = new Geometry("picture", new Quad(imageWidth, imageHeight, true));
        picture.setQueueBucket(RenderQueue.Bucket.Transparent);
        picture.setMaterial(material);

        if (glyph.getRenderMode() == FT_RENDER_MODE_SDF) {
            // the sdf image size is larger than glyph metrics size
            // image width = spread * 2 + width
            // image height = spread * 2 + height
            picture.move(-spread, -spread, 0);
        }
        return picture;
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        loadMetrics();
        loadImages();
    }
}