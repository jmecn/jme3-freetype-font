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
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import io.github.jmecn.font.generator.FtFontGenerator;
import io.github.jmecn.font.generator.FtFontParameter;
import io.github.jmecn.font.generator.enums.RenderMode;

import java.io.File;

public class TestBitmapText3DSdf extends SimpleApplication {

    final private String TEXT = "《将进酒·君不见》\n" +
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
            "五花马，千金裘，呼儿将出换美酒，与尔同销万古愁。";

    public static void main(String[] args){
        TestBitmapText3DSdf app = new TestBitmapText3DSdf();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);


        FtFontGenerator generator = new FtFontGenerator(new File("font/Noto_Serif_SC/NotoSerifSC-Regular.otf"));
        FtFontParameter parameter = new FtFontParameter();
        parameter.setSize(32);
        parameter.setCharacters(TEXT);

        parameter.setRenderMode(RenderMode.SDF);
        MaterialDef matDef = assetManager.loadAsset(new AssetKey<>("Shaders/Font/SdFont.j3md"));
        parameter.setMatDef(matDef);
        parameter.setSpread(2);
        //parameter.setUseVertexColor(false);
        parameter.setColorMapParamName("ColorMap");

        BitmapFont fnt = generator.generateFont(parameter);

        BitmapText txt = new BitmapText(fnt);
        txt.setSize(0.5f);
        txt.setText(TEXT);
        rootNode.attachChild(txt);
    }

}
