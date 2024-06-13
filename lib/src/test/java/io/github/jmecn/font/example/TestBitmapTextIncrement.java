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
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import io.github.jmecn.font.bmfont.FtBitmapCharacterSet;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.packer.Packer;
import io.github.jmecn.font.packer.strategy.SkylineStrategy;

import java.io.File;

public class TestBitmapTextIncrement extends SimpleApplication {

    private static final String TEXT = "《将进酒·君不见》\n" +
            "作者：李白 朝代：唐\n" +
            "君不见，黄河之水天上来，奔流到海不复回。\n" +
            "君不见，高堂明镜悲白发，朝如青丝暮成雪。\n" +
            "人生得意须尽欢，莫使金樽空对月。\n" +
            "天生我材必有用，千金散尽还复来。\n" +
            "烹羊宰牛且为乐，会须一饮三百杯。\n" +
            "岑夫子，丹丘生，将进酒，杯莫停。\n" +
            "与君歌一曲，请君为我倾耳听。\n" +
            "钟鼓馔玉不足贵，但愿长醉不愿醒。\n" +
            "古来圣贤皆寂寞，惟有饮者留其名。\n" +
            "陈王昔时宴平乐，斗酒十千恣欢谑。\n" +
            "主人何为言少钱，径须沽取对君酌。\n" +
            "五花马，千金裘，呼儿将出换美酒，与尔同销万古愁。 ";
    public static final int LENGTH = TEXT.length();
    public static final float SPEED = 0.1f;// seconds

    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(800, 600);
        settings.setSamples(4);
        TestBitmapTextIncrement app = new TestBitmapTextIncrement();
        app.setSettings(settings);
        app.start();
    }

    private BitmapText txt;
    private int current;
    private float timer;

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        flyCam.setEnabled(false);

        cam.setLocation(new Vector3f(4.029617f, -0.91866726f, 7.771896f));
        cam.setRotation(new Quaternion(-2.4323443E-5f, 0.99996126f, -0.0029299273f, -0.008300816f));

        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Common/MatDefs/Misc/Unshaded.j3md"));

        FtFontGenerator generator = new FtFontGenerator(new File("font/NotoSerifSC-Regular.otf"));

        // define a small texture size so it would be easier to generate new page
        Packer packer = new Packer(Image.Format.RGBA8, 128, 128, 1, false, new SkylineStrategy());

        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(16);
        parameter.setPacker(packer);
        parameter.setMatDef(matDef);
        parameter.setMagFilter(Texture.MagFilter.Bilinear);
        parameter.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        parameter.setBorderWidth(1);
        parameter.setCharacters("李白");
        parameter.setIncremental(true);// important

        FtBitmapCharacterSet characterSet = generator.generateData(parameter);
        BitmapFont fnt = generator.generateFont(parameter, characterSet);

        txt = new BitmapText(fnt);
        txt.setSize(0.2f);
        rootNode.attachChild(txt);

        // show font images
        int pageSize = fnt.getPageSize();
        for (int i = 0; i < pageSize; i++) {
            rootNode.attachChild(buildFontPage(fnt, i));
        }

        // add new page to the scene
        packer.addListener((packer1, strategy, page) -> {
            Geometry fontPage = buildFontPage(fnt, page.getIndex());
            enqueue(() -> rootNode.attachChild(fontPage));
        });
    }

    private Geometry buildFontPage(BitmapFont font, int i) {
        Geometry page = new Geometry("page#" + i, new Quad(2, 2));
        page.setLocalTranslation(i * 2, 0, 0);
        Material mat = font.getPage(i);
        page.setMaterial(mat);
        return page;
    }

    /**
     * Increment the text.
     */
    @Override
    public void simpleUpdate(float tpf) {
        timer += tpf;
        if (timer > SPEED) {
            timer = 0;
            if (current < LENGTH) {
                txt.setText(TEXT.substring(0, current++));
            }
        }
    }
}
