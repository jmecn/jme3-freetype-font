/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.github.jmecn.font.example;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.RenderMode;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;

import java.io.File;

public class TestBitmapText3D2 extends SimpleApplication {

    // 一段用于测试的文本
    static final String TEXT = "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?\n" +
            "jMonkeyEngine is a modern developer friendly game engine written primarily in Java.\n";

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);

        TestBitmapText3D2 app = new TestBitmapText3D2();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        buildBitmapText();
        buildFtBitmapText();
        buildSdfBitmapText();
    }

    private void buildSdfBitmapText() {
        Quad q = new Quad(6, 5);
        Geometry g = new Geometry("quad", q);
        g.setLocalTranslation(-6, -5, -0.0001f);
        g.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        rootNode.attachChild(g);

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Shaders/Font/SdFont.j3md"));
        FtFontGenerator generator = new FtFontGenerator(new File("font/FreeSerif.ttf"));
        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(new Packer(Image.Format.RGBA8, 256, 256, 0, false, new SkylineStrategy()));
        parameter.setSize(32);
        parameter.setRenderMode(RenderMode.SDF);
        parameter.setSpread(4);
        parameter.setMatDef(matDef);
        parameter.setCharacters(TEXT);

        BitmapFont fnt = generator.generateFont(parameter);

        BitmapText txt = new BitmapText(fnt);
        txt.setBox(new Rectangle(0, 0, 6, 5));
        txt.setQueueBucket(Bucket.Transparent);
        txt.setSize( 0.5f );
        txt.setText(TEXT);
        txt.setLocalTranslation(-6, 0, 0);
        rootNode.attachChild(txt);

        // show font images
        int pageSize = fnt.getPageSize();
        for (int i = 0; i < pageSize; i++) {
            Geometry g2 = buildFontPage(fnt, i);
            g2.setLocalTranslation(-6, i * 6, 0);
            rootNode.attachChild(g2);
        }
    }

    private Geometry buildFontPage(BitmapFont font, int i) {
        Geometry page = new Geometry("page#" + i, new Quad(6, 6));
        Material mat = font.getPage(i);
        Material unshaded = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        unshaded.setTexture("ColorMap", mat.getTextureParam("ColorMap").getTextureValue());
        page.setMaterial(unshaded);
        return page;
    }

    private void buildFtBitmapText() {
        Quad q = new Quad(6, 5);
        Geometry g = new Geometry("quad", q);
        g.setLocalTranslation(0, -5, -0.0001f);
        g.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        rootNode.attachChild(g);

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Common/MatDefs/Misc/Unshaded.j3md"));
        FtFontGenerator generator = new FtFontGenerator(new File("font/FreeSerif.ttf"));
        FtFontParameter parameter = new FtFontParameter();
        parameter.setPacker(new Packer(Image.Format.RGBA8, 256, 256, 0, false, new SkylineStrategy()));
        parameter.setSize(32);
        parameter.setMatDef(matDef);
        parameter.setMagFilter(Texture.MagFilter.Bilinear);
        parameter.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        parameter.setCharacters(TEXT);

        BitmapFont fnt = generator.generateFont(parameter);

        BitmapText txt = new BitmapText(fnt);
        txt.setBox(new Rectangle(0, 0, 6, 5));
        txt.setQueueBucket(Bucket.Transparent);
        txt.setSize( 0.5f );
        txt.setText(TEXT);
        rootNode.attachChild(txt);

        // show font images
        int pageSize = fnt.getPageSize();
        for (int i = 0; i < pageSize; i++) {
            Geometry g2 = buildFontPage(fnt, i);
            g2.setLocalTranslation(0, i * 6, 0);
            rootNode.attachChild(g2);
        }
    }

    private void buildBitmapText() {
        Quad q = new Quad(6, 5);
        Geometry g = new Geometry("quad", q);
        g.setLocalTranslation(6, -5, -0.0001f);
        g.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
        rootNode.attachChild(g);

        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(fnt);
        txt.setBox(new Rectangle(0, 0, 6, 5));
        txt.setQueueBucket(Bucket.Transparent);
        txt.setSize( 0.5f );
        txt.setText(TEXT);
        txt.setLocalTranslation(6, 0, 0);
        rootNode.attachChild(txt);

        // show font images
        int pageSize = fnt.getPageSize();
        for (int i = 0; i < pageSize; i++) {
            Geometry g2 = buildFontPage(fnt, i);
            g2.setLocalTranslation(6, i * 6, 0);
            rootNode.attachChild(g2);
        }
    }
}
